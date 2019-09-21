package qianhub.libs.http

import java.time.ZonedDateTime

import io.circe.{Encoder, Json}
import qianhub.libs.json.Jsons
import qianhub.libs.model.Magic._
import qianhub.libs.model.{ApiErrors, KeyID}

import scala.util.Try

object Jwt {

  implicit val JwtHeaderJson = Jsons.format[JwtHeader]

  // 目前只支持 HS256
  val HS256 = "HS256"

  val Empty = Json.obj()

  // 过期时间
  def hs256(key: KeyID, expired: ZonedDateTime): Jwt = {
    new Jwt(HS256, key, Empty, System.currentTimeMillis(), expired.toInstant.toEpochMilli)
  }

  // 解析 token，返回 Jwt 对象
  def decode(key: KeyID, token: String): Try[Jwt] = Try {
    val array = token.split("\\.")
    val content = array(0) + "." + array(1)
    val sign = content.bytes.sha256(key.value.bytes).base64
    if (sign == array(2)) {
      val header = array(0).base64.string
      val body = array(1).base64.string
      val h = Jsons.from[JwtHeader](header).get
      val b = Jsons.parse(body).get
      new Jwt(h.alg, key, b, h.iat, h.nbf)
    } else {
      throw ApiErrors.WrongData.extra(token)
    }
  }

}

final case class JwtHeader(alg: String, tpe: String, iat: Long, nbf: Long)

/**
 * 支持简易的 JWT(Json Web Token)
 * 和标准有一些改动:
 * 1) 将创建时间 iat 和过期时间 nbf 放到 header 中，这些与业务没有关系，不必放到 body 中
 * 2) body 中全部放置与业务相关的信息
 */
final case class Jwt private (algorithm: String, key: KeyID, body: Json, now: Long, expired: Long) {
  import Jwt._

  // 更新某个值
  def add[T](name: String, value: T)(implicit e: Encoder[T]): Jwt = {
    val real = body.mapObject(_.add(name, Jsons.toJson(value)))
    this.copy(body = real)
  }

  // 设置 Json 对象
  def replace(json: Json): Jwt = {
    this.copy(body = json)
  }

  // 删除某个值
  def remove(name: String): Jwt = {
    val real = body.mapObject(_.remove(name))
    this.copy(body = real)
  }

  // 转成 Token
  def token: String = {
    val h = JwtHeader(algorithm, "JWT", now, expired)
    val header = Jsons.toJson(h)
    val header64 = Jsons.stringify(header).bytes.base64
    val body64 = Jsons.stringify(body).bytes.base64
    val content = header64 + "." + body64
    val sign = content.bytes.sha256(key.value.bytes).base64
    content + "." + sign
  }

  // 是否合法
  def isValid: Boolean = expired > System.currentTimeMillis()
}
