package qianhub.libs.actor

import akka.actor._

import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

/**
 * Actor 基类
 */
trait ActorTrait extends Actor with ActorLogging {
  // 执行线程池
  implicit def EC: ExecutionContext = context.dispatcher
  implicit def system: ActorSystem = context.system

  def scheduler: Scheduler = context.system.scheduler

  // 按照顺序执行 Future, 并返回集合, 忽略失败, 减小 CPU 占用率
  final def futureOrder[T, R](seq: List[T], delay: FiniteDuration = Duration.Zero, exitOnError: Boolean = false)(
      next: T => Future[R]): Future[Seq[R]] = {
    val ret = Promise[Seq[R]]
    var retSeq = Seq.empty[R]
    def exec(sub: Seq[T]): Unit = sub match {
      case Nil => ret.success(retSeq)
      case head :: tail =>
        val start = System.currentTimeMillis()
        next(head).onComplete {
          case Failure(ex) if exitOnError =>
            log.error(s"futureOrder Error with $ex $head exit for exitOnError = true")
          case Failure(ex) =>
            log.error(s"futureOrder Error with $ex $head")
            exec(tail)
          case Success(r) =>
            log.debug(s"futureOrder OK with $head using ${System.currentTimeMillis() - start} millis")
            retSeq :+= r
            if (delay.length > 0) {
              scheduler.scheduleOnce(delay) { exec(tail) } // 一段时间后再调用
            } else {
              exec(tail)
            }
        }
    }
    exec(seq)
    ret.future
  }

  // 重试创建 Actor, 主要处理重名问题
  @scala.annotation.tailrec
  final def retryActor(props: Props, name: String, maxRetry: Int): Try[ActorRef] = {
    Try {
      context.child(name) match {
        case Some(ref) => ref
        case _         => context.watch(context.actorOf(props, name))
      }
    } match {
      case Failure(_) if maxRetry > 0 => retryActor(props, name, maxRetry - 1)
      case r =>
        if (r.isFailure) {
          log.error(s"retryActorError {} Result: {}", props, name)
        } else {
          log.debug(s"retryActorOK {} Result: {}", props, name)
        }
        r
    }
  }

  // 间隔周期重试
  final def retryDelay[T](maxRetry: Int, delay: FiniteDuration)(f: => Future[T]): Future[T] = {
    val p = Promise[T]
    def loop(i: Int): Unit = {
      f.onComplete {
        case Success(v) => p.success(v)
        case Failure(_) if i < maxRetry - 1 =>
          if (delay.length > 0) {
            scheduler.scheduleOnce(delay)(loop(i + 1))
          } else {
            loop(i + 1)
          }
        case Failure(ex) => p.failure(ex)
      }
    }
    loop(0)
    p.future
  }
}
