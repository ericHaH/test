package qianhub.libs.client

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import com.typesafe.scalalogging.Logger
import io.circe.{Decoder, Encoder}
import qianhub.libs.http.{CirceSupport, FormField}
import qianhub.libs.json.{Jsons, QianExceptionFormat}
import qianhub.libs.model.{ApiErrors, QianException}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
 *  客户端调用远程接口, 可以缓存链接，默认是 64 个，每个实例同时支持 65536 个请求
 *  说明:
 *  1) 一个 Host:Port 不管有多少 Client 实例，都会共享连接数和请求数
 *    max-connections = 64
 *    max-open-requests = 65536
 *  2) 默认参数已经很大了，可以满足绝大部分使用场景
 *  3) 如果并发请求比较多，可以修改 max-open-requests 和 ClientTrait 中的 defaultQueueSize
 */
trait ClientTrait {
  import CirceSupport._

  type Reply = (HttpRequest, Promise[HttpResponse])

  final val logger = Logger(this.getClass)

  implicit def ec: ExecutionContext
  implicit def system: ActorSystem

  implicit val QianExceptionFormat = new QianExceptionFormat()

  // 使用连接池处理，提高性能
  private final val pool =
    if (isSSL)
      Http().cachedHostConnectionPoolHttps[Promise[HttpResponse]](host, port)
    else
      Http().cachedHostConnectionPool[Promise[HttpResponse]](host, port)

  private final val queue = Source
    .queue[Reply](defaultQueueSize, OverflowStrategy.dropNew)
    .alsoTo(Sink.foreach { r =>
      // 由于 akka stream 不能被多次消费，因此在这里使用 alsoTo 记录请求的结构体. 最多取 1k 数据
      logger.whenDebugEnabled {
        val mt = r._1.entity.contentType.mediaType
        val ignore = mt.isAudio || mt.isImage || mt.isVideo
        if (!ignore) {
          r._1.entity.dataBytes.take(1024).runFold("")(_ ++ _.utf8String).foreach { data =>
            logger.debug("RequestBody: {} {}", r._1.uri, data)
          }
        }
      }
    })
    .via(pool)
    .toMat(Sink.foreach {
      case (Success(resp), p) => p.success(resp)
      case (Failure(e), p)    => p.failure(e)
    })(Keep.left)
    .run()

  // 访问接口路径，类似 https://www.google.com
  def website: String

  def toUrl(api: String): String = website + api

  // 超时时间, 默认 30 秒
  def defaultTimeout: FiniteDuration = 30.seconds

  // 默认队列大小
  def defaultQueueSize: Int = 1024

  // 请求头信息
  def httpHeaders: immutable.Seq[HttpHeader] = immutable.Seq.empty

  // 主机地址
  final def host: String = Uri(website).authority.host.address()

  // 主机端口号
  final def port: Int = Uri(website).effectivePort

  // 是否 HTTPS
  final def isSSL: Boolean = website.toLowerCase.startsWith("https://")

  // 包裹一下
  final def wrap[A](api: => String)(f: String => Future[A]): Future[A] = withUrl(toUrl(api))(f)

  // 执行之后会打印执行时间
  final def withUrl[A](url: String)(f: String => Future[A]): Future[A] = {
    val begin = System.currentTimeMillis
    f(url).andThen {
      case Success(_) =>
        val used = System.currentTimeMillis - begin
        logger.debug(s"SUCCESS Call $url Using $used millis")
      case Failure(ex) =>
        val used = System.currentTimeMillis - begin
        logger.error(s"FAILURE Call $url Using $used millis Error: $ex")
    }
  }

  // 调用请求接口
  final def singleRequest(request: HttpRequest): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    queue
      .offer(request -> responsePromise)
      .flatMap {
        case QueueOfferResult.Enqueued    => responsePromise.future
        case QueueOfferResult.Dropped     => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
        case QueueOfferResult.Failure(ex) => Future.failed(ex)
        case QueueOfferResult.QueueClosed =>
          Future.failed(
            new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
      }
      .andThen {
        case Success(reply) => logger.debug("Call: {} Reply:   {}", request.uri, reply.status)
        case Failure(ex)    => logger.warn(s"CallError: ${request.uri}", ex)
      }
  }

  // 调用 Get Json 接口
  final def getJson[Out](
      api: String,
      headers: immutable.Seq[HttpHeader] = httpHeaders,
      timeout: FiniteDuration = defaultTimeout)(implicit d: Decoder[Out]): Future[Out] = {
    wrap(api) { url =>
      val request = HttpRequest(HttpMethods.GET, url, headers)
      //singleRequest(request).flatMap(r => toObject[Out](r))
      singleRequest(request).flatMap { r =>
        r.status match {
          case StatusCodes.OK         => Unmarshal(r.entity.withContentType(ContentTypes.`application/json`)).to[Out]
          case StatusCodes.BadRequest => Unmarshal(r.entity).to[QianException].flatMap(ex => Future.failed(ex))
          case _                      => Future.failed(ApiErrors.InvalidJson)
        }
      }
    }
  }

  // 调用 Post Json 接口
  final def postJson[In, Out](
      api: String,
      headers: immutable.Seq[HttpHeader] = httpHeaders,
      timeout: FiniteDuration = defaultTimeout)(value: In)(implicit e: Encoder[In], d: Decoder[Out]): Future[Out] = {
    wrap(api) { url =>
      val body = HttpEntity(MediaTypes.`application/json`, Jsons.stringify(value))
      val request = HttpRequest(HttpMethods.POST, url, headers, entity = body)
      logger.debug("HttpRequest: {}", request)
      singleRequest(request).flatMap(r => toObject[Out](r))
    }
  }

  // 调用 Post Json 接口
  final def postTextAsJson[Out](
      api: String,
      headers: immutable.Seq[HttpHeader] = httpHeaders,
      timeout: FiniteDuration = defaultTimeout)(value: String)(implicit d: Decoder[Out]): Future[Out] = {
    wrap(api) { url =>
      val body = HttpEntity(MediaTypes.`application/json`, value)
      val request = HttpRequest(HttpMethods.POST, url, headers, entity = body)
      logger.debug("HttpRequest: {}", request)
      singleRequest(request).flatMap(r => toObject[Out](r))
    }
  }

  // 上传表单数据, 包含了文件和字符串
  final def postForm(
      api: String,
      headers: immutable.Seq[HttpHeader] = httpHeaders,
      timeout: FiniteDuration = defaultTimeout)(fields: Seq[FormField]): Future[HttpResponse] = {
    wrap(api) { url =>
      val forms = fields.map {
        case f if f.isValue =>
          Multipart.FormData.BodyPart.Strict(f.field, f.value.get)
        case f =>
          Multipart.FormData.BodyPart.fromFile(f.field, f.contentType.get, f.target.get)
      }
      val formData = Multipart.FormData(forms: _*)
      Marshal(formData).to[RequestEntity].flatMap { entity =>
        val req = HttpRequest(HttpMethods.POST, url, headers, entity = entity)
        singleRequest(req)
      }
    }
  }

  // 转成对象
  protected def toObject[Out](resp: HttpResponse)(implicit d: Decoder[Out]): Future[Out] = {
    resp.status match {
      case StatusCodes.OK         => Unmarshal(resp.entity).to[Out]
      case StatusCodes.BadRequest => Unmarshal(resp.entity).to[QianException].flatMap(ex => Future.failed(ex))
      case _                      => Future.failed(ApiErrors.InvalidJson)
    }
  }

  // 退出后关闭连接
  def close(): Future[Done] = {
    queue.complete()
    queue.watchCompletion()
  }

}
