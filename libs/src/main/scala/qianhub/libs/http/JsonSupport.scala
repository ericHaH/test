package qianhub.libs.http

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.circe._
import qianhub.libs.json.Jsons
import qianhub.libs.model.ApiErrors
import qianhub.libs.model.Magic._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

// 针对 Json 的操作
trait JsonSupport { self: ServerSupport =>

  // 普通字符串返回值
  final def text(str: String): Route =
    _.complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(str)))

  // HTML 页面
  final def html(str: String): Route =
    _.complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, str)))

  // 返回 Json 对象
  final def JResult[T](data: T)(implicit d: Encoder[T]): HttpResponse = {
    val json = Jsons.toJson(data)
    HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, Jsons.stringify(json)))
  }

  final def JResult(ex: Throwable)(implicit ctx: RequestContext): HttpResponse = {
    val q = ex match {
      case ex: DecodingFailure             => ApiErrors.InvalidJson.extra(ex.getMessage())
      case Unmarshaller.NoContentException => ApiErrors.NoJson
      case _                               => ex.base
    }
    val json = toJson(i18n(q))
    HttpResponse(
      StatusCodes.BadRequest,
      entity = HttpEntity(ContentTypes.`application/json`, Jsons.stringify(json)))
  }

  // 成功请求
  final def JOk[T](data: T)(implicit d: Encoder[T]): Route = _.complete(JResult(data))

  // 失败请求
  final def JBad(ex: Throwable): Route = ctx => ctx.complete(JResult(ex)(ctx))

  // 将 Model 转成 Json
  implicit final def Model2Json[A](a: A)(implicit d: Encoder[A]): Json = d(a)

  // 将 Future[A] 转成 Future[Json]
  implicit final def FutureModel2Json[A](a: Future[A])(implicit ec: ExecutionContext, d: Encoder[A]): Future[Json] =
    a.map(r => d(r))

  // 将 Future 转成 ToResponseMarshallable
  implicit final def Future2Marshallable[A](
      a: Future[A])(implicit ec: ExecutionContext, rc: RequestContext, d: Encoder[A]): ToResponseMarshallable = {
    a.map(r => JResult(r)).recover {
      case ex => JResult(ex)
    }
  }

  // 直接将 Model 返回给 Http
  implicit final def Future2Route[A](a: Future[A])(implicit ec: ExecutionContext, d: Encoder[A]): Route = {
    implicit ctx =>
      a.flatMap { r =>
          ctx.complete(JResult(r))
        }
        .recoverWith {
          case ex => ctx.complete(JResult(ex))
        }
  }

  // 根据请求分解出 Form
  final def routeForm[I, O](f: I => Future[O])(implicit ec: ExecutionContext, d: Decoder[I], e: Encoder[O]): Route = {
    implicit ctx =>
      import ctx.materializer
      val um = as[I]
      val q = for {
        t <- um(ctx.request)
        v <- f(t)
      } yield Jsons.toJson(v)
      ctx.complete(q)
  }

  // 根据请求分解出 Form, 请求返回 Route
  final def routeForm[I](f: I => Future[Route])(implicit ec: ExecutionContext, d: Decoder[I]): Route = { implicit ctx =>
    import ctx.materializer
    val um = as[I]
    for {
      t <- um(ctx.request)
      v <- f(t)
      r <- v(ctx)
    } yield r
  }

  // 获取对象(参数中有隐式，因此请用 apply 调用)
  final def extractForm[I, O](f: I => Future[O])(implicit ec: ExecutionContext, d: Decoder[I]): Directive1[O] = {
    extractRequestContext.flatMap { ctx =>
      import ctx.materializer
      val um = as[I]
      val q = for {
        t <- um(ctx.request)
        v <- f(t)
      } yield v
      onSuccess(q)
    }
  }

  // 根据传入的 Body 分解出 Form
  final def withForm[I, O](body: String)(f: I => Future[O])(implicit ec: ExecutionContext, d: Decoder[I]): Future[O] = {
    logger.debug("withMessage: {}", body)
    val q = for {
      b <- Future.fromTry(Jsons.from[I](body))
      r <- f(b)
    } yield r
    q.andThen {
      case Success(value) => logger.debug("FormResult: {}", value)
      case Failure(ex)    => logger.warn("FromError: {} ", body, ex)
    }
  }
}
