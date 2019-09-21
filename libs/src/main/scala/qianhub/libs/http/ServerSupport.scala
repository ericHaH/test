package qianhub.libs.http

import akka.http.scaladsl.server.Directives

// 处理一些与 MVC 有关的事宜
trait ServerSupport
    extends Directives
    with LoggerSupport
    with ErrorSupport
    with I18nSupport
    with CirceSupport
    with JsonSupport
    with FileSupport
    with WebSocketSupport
    with CookieSupport
