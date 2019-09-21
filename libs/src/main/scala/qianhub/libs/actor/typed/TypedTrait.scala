package qianhub.libs.actor.typed

import akka.actor.typed._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.slf4j.Logger

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

// 基于 typed actor 的一些操作
trait TypedTrait[T] {

  final type Receive = PartialFunction[T, Unit]

  implicit def EC: ExecutionContext = context.executionContext

  implicit def scheduler: Scheduler = context.system.scheduler

  // 上下文
  def context: ActorContext[T]

  def logger: Logger = context.log

  // 执行消息
  def receive: Receive

  // 处理系统事件
  def onSignal: PartialFunction[Signal, Unit] = PartialFunction.empty

  // 生成 Behavior
  def behavior: Behavior[T] = {
    val r = receive
    Behaviors
      .receiveMessage[T] {
        case t if r.isDefinedAt(t) =>
          r(t)
          Behaviors.same
        case t =>
          logger.warn("{} not handled", t)
          Behaviors.unhandled
      }
      .receiveSignal {
        case (_, signal) if onSignal.isDefinedAt(signal) =>
          onSignal(signal)
          Behaviors.same
        case _ => Behaviors.unhandled
      }
  }

  // 重试创建 Actor, 主要处理重名问题
  @scala.annotation.tailrec
  final def retryActor[U](behavior: Behavior[U], name: String, maxRetry: Int): Try[ActorRef[U]] = {
    Try {
      context.child(name) match {
        case Some(ref) => ref.unsafeUpcast
        case _ =>
          val ref = context.spawn(behavior, name)
          context.watch(ref)
          ref
      }
    } match {
      case Failure(_) if maxRetry > 0 => retryActor(behavior, name, maxRetry - 1)
      case r =>
        if (r.isFailure) {
          logger.error(s"retryActorError {} Result: {}", behavior, name)
        } else {
          logger.debug(s"retryActorOK {} Result: {}", behavior, name)
        }
        r
    }
  }

  // 按照顺序执行 Future, 并返回集合, 忽略失败, 减小 CPU 占用率
  final def futureOrder[I, O](seq: List[I], delay: FiniteDuration = Duration.Zero, exitOnError: Boolean = false)(
      next: I => Future[O]): Future[Seq[O]] =
    TypedHelper.futureOrder(seq, delay, exitOnError)(next)

  // 间隔周期重试
  final def retryDelay[U](maxRetry: Int, delay: FiniteDuration)(f: => Future[U]): Future[U] =
    TypedHelper.retryDelay(maxRetry, delay)(f)
}
