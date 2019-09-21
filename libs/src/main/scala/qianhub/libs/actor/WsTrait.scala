package qianhub.libs.actor

import java.time.{Instant, ZonedDateTime}

import akka.actor.ActorRef
import io.circe._
import qianhub.libs.actor.WsTrait._
import qianhub.libs.json._
import qianhub.libs.model._

import scala.collection.concurrent.TrieMap
import scala.concurrent._
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util._

/**
 * WebSocket Actor 基类
 */
object WsTrait {
  // 返回值
  final private[actor] case class AskResult(value: Promise[Any], createdAt: ZonedDateTime = ZonedDateTime.now)
}

trait WsTrait { self: ActorTrait =>

  type In <: WsTo
  type Out <: WsTo

  // 负责发送消息
  def outRef: ActorRef

  val Default = 30.seconds

  // 请求信息
  protected[this] val askMap = TrieMap.empty[SN, AskResult]

  // 需要处理返回值
  def send[T](action: String, message: T, timeout: FiniteDuration = Default)(
      implicit w: Encoder[T],
      format: WsFormat[Out]): Future[Any] = {
    val result = Promise[Any]
    val sn = SN(Randoms.genToken)
    val req = format.from(action, Option(Jsons.toJson(message)), None, Option(sn))
    askMap.put(req.tag.get, AskResult(result)) // 缓存结果
    outRef ! req
    val f = scheduler.scheduleOnce(timeout) {
      result.failure(ApiErrors.Timeout)
    }
    result.future.andThen {
      case _ =>
        f.cancel
        askMap.remove(req.tag.get).foreach { r =>
          log.debug(s"$action using {} millis", Instant.now().toEpochMilli - r.createdAt.toInstant.toEpochMilli)
        }
    }
  }

  // 收到确认消息了
  def ack[T](message: Json, tagOpt: Option[SN] = None)(implicit reads: Decoder[T], classTag: ClassTag[T]): Future[T] = {
    // 获取返回值
    val tryResult = Jsons
      .from[T](message)
      .fold(errors => {
        val ex = ApiErrors.InvalidJson.extra(errors.toString)
        log.warning(s"Send Error WsToCenter: $ex")
        Failure(ex)
      }, data => Success(data))
    val q = for {
      tag <- tagOpt
      result <- askMap.get(tag)
    } yield result
    // 看是否有等待者
    q match {
      case Some(result) =>
        result.value.tryComplete(tryResult)
        result.value.future.mapTo[T]
      case None => Future.fromTry(tryResult)
    }
  }

  // 收到失败消息了
  def fail[T](error: Json, tagOpt: Option[SN] = None)(
      implicit reads: Decoder[T],
      classTag: ClassTag[T],
      d: Decoder[QianException]): Future[T] = {
    val tryResult = Jsons
      .from[QianException](error)
      .fold(errors => Failure(ApiErrors.SystemError.extra(errors.toString)), data => Failure(data))
    val q = for {
      tag <- tagOpt
      result <- askMap.get(tag)
    } yield result
    // 看是否有等待者
    q match {
      case Some(result) =>
        result.value.tryComplete(tryResult)
        result.value.future.mapTo[T]
      case None => Future.fromTry(tryResult)
    }
  }

  // 发出去, 不理会返回值(没有请求内容)
  def post(action: String)(implicit format: WsFormat[Out]): Unit = {
    val to = format.from(action, None, None, None)
    outRef ! to
  }

  // 发出去, 不理会返回值
  def post[T](action: String, message: T)(implicit w: Encoder[T], format: WsFormat[Out]): Unit = {
    val to = format.from(action, Option(Jsons.toJson(message)), None, None)
    outRef ! to
  }

  // 发送异常消息
  def postError(action: String, ex: Throwable)(implicit e: Encoder[QianException], format: WsFormat[Out]): Unit = {
    val to = format.from(action, None, Some(toJson(ex)), None)
    outRef ! to
  }

  // 异常转成 Json
  def toJson(ex: Throwable)(implicit e: Encoder[QianException]): Json = ex match {
    case q: QianException => Jsons.toJson(q)
    case other            => toJson(ApiErrors.SystemError.extra(other))
  }
}
