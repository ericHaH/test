package qianhub.libs.boot

import akka.actor.typed._
import akka.actor.typed.scaladsl._
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._

// 返回需要绑定的 Http 服务
final case class HttpRoute(
    host: String,
    port: Int,
    route: Route,
    rejectionHandler: RejectionHandler,
    exceptionHandler: ExceptionHandler)

// 加载服务
trait ServiceLoader {
  // 根据掺入的 ActorContext 加载服务
  def load(context: ActorContext[Nothing]): ActorService
}

// 服务接口
trait ActorService extends AutoCloseable {

  // 服务名称
  def serviceName: String

  // 返回的 HTTP 路由，如果没有就返回 None
  def httpRoute: Option[HttpRoute] = Option.empty

}

// 如果系统是 Typed ActorSystem，需要使用 TypedBoot 来启动
trait TypedBoot extends Boot {

  // 启动根节点
  final def guardian(loader: ServiceLoader): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val Classic = ctx.system.toClassic
    implicit val EC = ctx.system.executionContext
    val service = loader.load(ctx)
    val binding = service.httpRoute.map { r =>
      implicit val rejectHandler = r.rejectionHandler
      implicit val exceptionHandler = r.exceptionHandler
      // 启动 server
      Http().bindAndHandle(r.route, r.host, r.port).andThen {
        case t => ctx.log.info("Bind: {}:{} Result {}", r.host, r.port, t)
      }
    }
    Behaviors.receiveSignal[Nothing] {
      case (_, PostStop) =>
        ctx.log.info("System is stopping")
        binding.foreach { t =>
          t.flatMap(_.unbind()).onComplete { _ =>
            Http().shutdownAllConnectionPools()
          }
        }
        service.close()
        ctx.log.info("System stopped")
        Behaviors.stopped
    }
  }

}
