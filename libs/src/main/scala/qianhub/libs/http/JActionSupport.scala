package qianhub.libs.http

import akka.http.scaladsl.server.Route
import io.circe.Json
import qianhub.libs.model.HttpAction

import scala.concurrent.Future

// 支持 JAction 操作
abstract class JActionSupport[T] extends ServerSupport {

  final type JAction = PartialFunction[HttpAction, T => Future[Json]]

  // 子 Route, 如果包含就需要重载该变量
  val Routes = Seq.empty[JActionSupport[T]]

  // 处理 Json 消息
  def jsonHandler: JAction = PartialFunction.empty

  // 其他路由
  def otherRoute: Route = reject

  // 聚合
  final def joinHandler: JAction = Routes.foldLeft(jsonHandler) { case (a, b) => a.orElse(b.joinHandler) }

  // 聚合
  final def joinRoute: Route = Routes.foldLeft(otherRoute) { case (a, b) => a ~ b.joinRoute }
}
