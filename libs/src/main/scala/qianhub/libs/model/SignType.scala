package qianhub.libs.model

// 签名类型
final case class SignType(value: String) extends AnyVal with StringMID

object SignType {
  val None = new SignType("None") // 不签名
  val HmacSHA1 = new SignType("HmacSHA1")
  val HmacSHA256 = new SignType("HmacSHA256")
  val SHA1 = new SignType("SHA-1")
  val SHA256 = new SignType("SHA-256")
  val MD5 = new SignType("MD5")
}
