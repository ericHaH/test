package qianhub.libs.model

/**
 * 编码，可供人识别的 一般是 前缀(可选) + 数字
 */
final case class SN(value: String) extends AnyVal with StringMID

object SN {

  // 空值
  val Z = new SN("")

  // 默认 12 位编码, prefix 是业务编码, 后面随机数字
  def of(prefix: String = "", size: Int = 12): SN = SN(prefix + Randoms.genNumString(size))

}
