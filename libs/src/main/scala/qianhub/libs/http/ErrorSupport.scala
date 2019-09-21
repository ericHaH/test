package qianhub.libs.http

import akka.http.scaladsl.server._
import qianhub.libs.model.ApiErrors

import scala.language.implicitConversions

trait ErrorSupport { self: ServerSupport =>

  implicit final def Exception2Rejection(ex: Throwable): Rejection = QianRejection(ex)

  // 处理拒绝消息
  final def rejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle(otherRejection)
      .handleNotFound {
        extractUnmatchedPath { p =>
          JBad(ApiErrors.NotFound.extra(p.toString()))
        }
      }
      .handle {
        case r: QianRejection => JBad(r.ex)
        case _: MissingCookieRejection =>
          extractUri { p =>
            JBad(ApiErrors.NoLogin.extra(p.toString()))
          }
        case t: Rejection =>
          logger.error("HandleUrlError: {}", t)
          extractUri { p =>
            JBad(ApiErrors.SystemError.extra(t.toString))
          }
      }
      .result()

  // 其他的异常处理
  def otherRejection: PartialFunction[Rejection, Route] = PartialFunction.empty

  // 处理异常消息
  final def exceptionHandler: ExceptionHandler = otherException.orElse {
    ExceptionHandler {
      case ex =>
        extractUri { uri =>
          logger.warn(s"$uri Error", ex)
          JBad(ex)
        }
    }
  }

  // 扩展的异常处理
  def otherException: PartialFunction[Throwable, Route] = PartialFunction.empty
}
