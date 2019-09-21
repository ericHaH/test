package qianhub.libs.actor

import akka.actor._

/**
 * 最简易的事件通知
 * 有以下约束:
 * 1) 事件对象只能发给本地的 Actor
 * 2）只支持 Fire-Forget 模式, 即不处理返回值
 */
object EventActor {

  // 创建 Actor
  def props(): Props = Props(new EventActor())

  // 增加 Sub
  final case class AddSub(ref: ActorRef)
  // 删除 Sub
  final case class RemoveSub(ref: ActorRef)
}

final class EventActor extends ActorTrait {
  import EventActor._

  var refSet = Set.empty[ActorRef]

  override def receive: Receive = {
    case AddSub(ref) =>
      refSet += ref
      context.watch(ref)
      log.info("AddSub: {}", ref)
    case RemoveSub(ref) =>
      refSet -= ref
      context.unwatch(ref)
      log.info("RemoveSub: {}", ref)
    case Terminated(ref) =>
      refSet -= ref
      log.info("Terminated {}", ref)
    case t => notifyEvent(t)
  }

  def notifyEvent(t: Any): Unit = {
    log.debug("NotifyEvent: {}", t)
    refSet.foreach { ref => ref ! t }
  }
}
