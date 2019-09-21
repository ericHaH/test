package qianhub.libs.http

import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server._
import io.circe.{Decoder, Encoder, Json}
import qianhub.libs.json.Jsons

import scala.util.Success

// 处理 Cookie
trait CookieSupport { self: ServerSupport =>

  // Cookie 配置
  implicit def cookieConfig: CookieConfig

  // 将整个 cookie 转成 Jwt, 如果没有，就返回空的 Jwt 实例
  final def cookie2Jwt: Directive1[Jwt] = {
    optionalCookie(cookieConfig.name).map {
      case Some(p) =>
        Jwt.decode(cookieConfig.key, p.value) match {
          case Success(jwt) if jwt.isValid => jwt
          case _                           => Jwt.hs256(cookieConfig.key, cookieConfig.expired)
        }
      case None => Jwt.hs256(cookieConfig.key, cookieConfig.expired)
    }
  }

  // 将整个 cookie 转成 Json
  final def cookie2Json: Directive1[Json] = cookie2Jwt.map(_.body)

  // 获取 cookie 中的某个值(参数中有隐式，因此请用 apply 调用)
  final def cookieOf[T](name: String)(implicit d: Decoder[T]): Directive1[Option[T]] = {
    cookie2Json.map { json =>
      json.hcursor.downField(name).focus.flatMap { r =>
        Jsons.from(r).toOption
      }
    }
  }

  // 获取 cookie 中的某个 String 值
  final def cookieOfString(name: String): Directive1[Option[String]] = cookieOf(name)

  // 批量新建 cookie(会废弃旧的 cookie)
  final def newCookies(first: (String, Json), more: (String, Json)*): Directive1[Json] = {
    cookie2Jwt.flatMap { jwt =>
      val real = (first :: more.toList).foldLeft(jwt.replace(Jwt.Empty)) { case (j, (k, v)) => j.add(k, v) }
      setJwt(real)
    }
  }

  // 增加值(参数中有隐式，因此请用 apply 调用)
  final def addCookie[T](name: String, value: T)(implicit encoder: Encoder[T]): Directive1[Json] =
    addCookies((name, Jsons.toJson(value)))

  // 批量增加值
  final def addCookies(first: (String, Json), more: (String, Json)*): Directive1[Json] = {
    cookie2Jwt.flatMap { jwt =>
      val real = (first :: more.toList).foldLeft(jwt) { case (j, (k, v)) => j.add(k, v) }
      setJwt(real)
    }
  }

  // 删除某些 cookie
  final def removeCookies(name: String, more: String*): Directive1[Json] = {
    cookie2Jwt.flatMap { jwt =>
      val real = (name :: more.toList).foldLeft(jwt) { case (j, n) => j.remove(n) }
      setJwt(real)
    }
  }

  // 直接设置 JWT 对象
  final def setJwt(jwt: Jwt): Directive1[Json] =
    setCookie(HttpCookie(cookieConfig.name, jwt.token, httpOnly = true)).tmap(_ => jwt.body)

  // 删除
  final def invalidateCookie: Directive0 = deleteCookie(HttpCookie(cookieConfig.name, "", httpOnly = true))
}
