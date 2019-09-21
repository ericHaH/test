package qianhub.libs.http

import java.util.Locale

import akka.http.scaladsl.model.headers.Language
import akka.http.scaladsl.server.{LanguageNegotiator, RequestContext}
import io.circe.Json
import io.circe.syntax._
import qianhub.libs.i18n.{Lang, Langs}
import qianhub.libs.model.Magic._
import qianhub.libs.model.QianException

// 对国际化提供支持
trait I18nSupport {

  implicit def langs: Langs

  implicit final def RequestContext2Lang(implicit ctx: RequestContext): Lang = {
    val codes = langs.codes.map(Language.apply)
    val l = LanguageNegotiator(ctx.request.headers).pickLanguage(codes).getOrElse(codes.head)
    langs.find(Locale.forLanguageTag(l.primaryTag))
  }

  // 国际化
  final def i(key: String, args: Any*)(implicit lang: Lang): String = lang.i(key, args)

  // 国际化异常
  final def i18n(throwable: Throwable)(implicit lang: Lang): QianException = {
    val q = throwable.base
    val i18nMessage = lang.i(s"error.${q.errorCode}")
    val message = if (i18nMessage.startsWith("error.")) q.message else i18nMessage
    val extraMessage = q.extra.map(r => lang.i(r))
    q.copy(message = message, extra = extraMessage)
  }

  // 必须是国际化后的对象
  final def toJson(q: QianException): Json = {
    Json.obj("error_code" -> q.errorCode.asJson, "message" -> q.message.asJson, "extra_message" -> q.extra.asJson)
  }
}
