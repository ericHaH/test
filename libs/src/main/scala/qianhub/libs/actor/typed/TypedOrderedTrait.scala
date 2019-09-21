package qianhub.libs.actor.typed

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.Future

// 顺序执行的 Actor
trait TypedOrderedTrait[T] extends TypedTrait[T] {

  final type FutureReceive = PartialFunction[T, Future[_]]

  // 最多缓存的消息大小
  def stashSize: Int = 1024

  // 结束等待的标识
  def endWaiting: T

  // 按顺序执行, 警告：在该流程中不能再向自己发送异步消息，否则会出现死锁
  def orderedReceive: FutureReceive = PartialFunction.empty

  // 接收消息
  final override def behavior: Behavior[T] = Behaviors.setup { ctx =>
    Behaviors.withStash(stashSize) { stash =>
      val EndWaiting = endWaiting
      val anytime = receive
      val ordered = orderedReceive
      // 等待消息
      def waitingReceive: Behavior[T] = {
        Behaviors.receiveMessage[T] {
          case EndWaiting =>
            stash.unstashAll(behavior)
          case t if anytime.isDefinedAt(t) =>
            anytime(t)
            Behaviors.same
          case t =>
            stash.stash(t)
            Behaviors.same
        }
      }
      // 处理消息
      Behaviors
        .receiveMessage[T] {
          case t if anytime.isDefinedAt(t) =>
            anytime(t)
            Behaviors.same
          case t if ordered.isDefinedAt(t) =>
            ordered(t).andThen {
              case _ => ctx.self ! EndWaiting
            }(ctx.executionContext)
            waitingReceive
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
  }
}
