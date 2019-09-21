package qianhub.libs.actor.typed

import java.util.concurrent.ThreadLocalRandom

import akka.actor.typed.receptionist.Receptionist.Listing
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

// 一些帮助函数
object TypedHelper {

  private val logger = Logger(this.getClass)

  // 按照顺序执行 Future, 并返回集合, 忽略失败, 减小 CPU 占用率
  final def futureOrder[I, O](seq: List[I], delay: FiniteDuration = Duration.Zero, exitOnError: Boolean = false)(
      next: I => Future[O])(implicit ec: ExecutionContext, scheduler: Scheduler): Future[Seq[O]] = {
    val ret = Promise[Seq[O]]
    var retSeq = Seq.empty[O]
    def exec(sub: Seq[I]): Unit = sub match {
      case Nil => ret.success(retSeq)
      case head :: tail =>
        val start = System.currentTimeMillis()
        next(head).onComplete {
          case Failure(ex) if exitOnError =>
            logger.error(s"futureOrder Error with $ex $head exit for exitOnError = true")
          case Failure(ex) =>
            logger.error(s"futureOrder Error with $ex $head")
            exec(tail)
          case Success(r) =>
            logger.debug(s"futureOrder OK with $head using ${System.currentTimeMillis() - start} millis")
            retSeq :+= r
            if (delay.length > 0) {
              scheduler.scheduleOnce(delay, () => exec(tail)) // 一段时间后再调用
            } else {
              exec(tail)
            }
        }
    }
    exec(seq)
    ret.future
  }

  // 间隔周期重试
  final def retryDelay[U](maxRetry: Int, delay: FiniteDuration)(
      f: => Future[U])(implicit ec: ExecutionContext, scheduler: Scheduler): Future[U] = {
    val p = Promise[U]
    def loop(i: Int): Unit = {
      f.onComplete {
        case Success(v) => p.success(v)
        case Failure(_) if i < maxRetry - 1 =>
          if (delay.length > 0) {
            scheduler.scheduleOnce(delay, () => loop(i + 1))
          } else {
            loop(i + 1)
          }
        case Failure(ex) => p.failure(ex)
      }
    }
    loop(0)
    p.future
  }

  private final case class WrapListing[T](services: Set[ActorRef[T]])

  def randomRouter[T](serviceKey: ServiceKey[T]): Behavior[T] =
    Behaviors
      .setup[Any] { ctx =>
        val listingAdapter = ctx.messageAdapter[Listing] { case serviceKey.Listing(services) => WrapListing(services) }
        ctx.system.receptionist ! Receptionist.Subscribe(serviceKey, listingAdapter)
        // 随机选择路由
        def routing(routees: Vector[ActorRef[T]]): Behavior[Any] =
          Behaviors.receive { (ctx, msg) =>
            msg match {
              case WrapListing(services) =>
                ctx.log.info("ServiceKey: {} {}", serviceKey, services)
                routing(services.toVector)
              case other: T @unchecked =>
                if (routees.isEmpty)
                  Behaviors.unhandled
                else {
                  val i = if (routees.size == 1) 0 else ThreadLocalRandom.current.nextInt(routees.size)
                  routees(i) ! other
                  Behaviors.same
                }
            }
          }
        routing(Vector.empty)
      }
      .narrow[T]
}
