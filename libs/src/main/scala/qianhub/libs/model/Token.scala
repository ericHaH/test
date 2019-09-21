package qianhub.libs.model

import java.util.UUID

import qianhub.libs.model.Magic._

/**
 * 会话 Token, 可供机器识别的， 一般是 前缀(可选) + 时间戳(可选) + 字符串
 */
final case class Token(value: String) extends AnyVal with StringMID

object Token {

  val Z = Token("")

  def uuid: Token = Token(UUID.randomUUID().toString)

  // 前缀+随机字符
  def string(prefix: String = "", size: Int = 12): Token = Token(prefix + Randoms.genString(size))

  // 前缀 + yyyyMMddHHmmss + 随机字符
  def date(prefix: String = "", size: Int = 8): Token = Token(prefix + Randoms.genDate + Randoms.genString(size))

  // 毫秒转换成 62 进制，减小大小
  def millis(prefix: String = "", size: Int = 8): Token =
    Token(prefix + System.currentTimeMillis().base62 + Randoms.genString(size))
}
