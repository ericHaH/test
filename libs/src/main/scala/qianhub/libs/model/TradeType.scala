package qianhub.libs.model

/**
 * 交易类型
 */
final case class TradeType(value: String) extends AnyVal with StringMID {
  // 生成随机的 TradeID
  def trade: TradeID = TradeID.of(value)
}

object TradeType {
  // 收银
  val Cashier = TradeType("CASHIER")
  // 会员充值
  val Charge = TradeType("CHARGE")
  // 购买
  val Shop = TradeType("SHOP")
  // 提现
  val Withdraw = TradeType("WITHDRAW")
  // 服务费
  val Service = TradeType("Service")
  // 输入金额支付
  val InputPay = TradeType("InputPay")
}
