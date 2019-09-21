package qianhub.libs.actor

import akka.actor._

import scala.concurrent.Future

// 按顺序处理消息(消息处理流程是异步时)
trait OrderedActorTrait extends ActorTrait with Stash {

  type FutureReceive = PartialFunction[Any, Future[_]]

  // 任何时候都需要处理的消息
  def anytimeReceive: Receive = PartialFunction.empty

  // 需要按顺序处理的消息(其下的消息不能互相调用，否则会出现死锁)
  def orderReceive: FutureReceive = PartialFunction.empty

  final def receive: Receive = anytimeReceive.orElse {
    case r if orderReceive.isDefinedAt(r) =>
      context.become(waitingReceive, discardOld = false)
      orderReceive(r).andThen {
        case _ => self ! EndWaiting
      }(context.dispatcher)
  }

  final def waitingReceive: Receive = anytimeReceive.orElse {
    // 结束等待
    case EndWaiting =>
      context.unbecome()
      unstashAll()
    case _ => stash()
  }
}
