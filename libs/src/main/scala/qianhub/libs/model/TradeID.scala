package qianhub.libs.model

/**
 * 支付的交易号, 可供机器识别的 前缀(可选) + 时间戳 + 随机字符串
 */
final case class TradeID(value: String) extends AnyVal with StringMID

// 交易号
object TradeID {

  val Z = TradeID("")

  // 32 位支付交易号, prefix + 14 位时间戳 + 8 位随机字母, prefix < 11
  def of(prefix: String = "", size: Int = 8): TradeID = TradeID(prefix + Randoms.genDate + Randoms.genString(size))

  // 生成简单的四位验证码
  def genAckCode(seed: Long): String = {
    val ret = (math.abs(math.cos(seed)) * 10000).toInt
    f"$ret%04d"
  }
}
