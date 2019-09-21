package qianhub.libs.actor

import akka.actor.ReceiveTimeout

import scala.concurrent.duration._

// 处理无用消息的 Actor
final class DummyActor(timeout: FiniteDuration = 5.minutes) extends ActorTrait {

  override def preStart(): Unit = {
    context.setReceiveTimeout(timeout)
  }

  override def receive: Receive = {
    case ReceiveTimeout => context.stop(self) // 放在前面处理
    case t              => log.warning("UnknownMessage: {}", t)
  }
}
