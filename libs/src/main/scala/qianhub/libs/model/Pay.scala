package qianhub.libs.model

// 支付渠道类型，一般对应公司, 需要支持可扩展
final case class PayChannel(value: String) extends AnyVal with StringMID

// 支持的支付通道
object PayChannel {
  val None = PayChannel("None") // 无通道，不支持支付
  val Alipay = PayChannel("Alipay") // 支付宝官方
  val Weixin = PayChannel("Weixin") // 微信官方
  val Swift = PayChannel("Swift") // 威富通
  val Fuiou = PayChannel("Fuiou") // 富友
  val Helibao = PayChannel("Helibao") // 合利宝

  implicit class RichPayChannel(val value: PayChannel) {
    def isEmpty: Boolean = value == None
    def nonEmpty: Boolean = !isEmpty
    def isWeixin: Boolean = value == Weixin
    def isAlipay: Boolean = value == Alipay
    def isSwift: Boolean = value == Swift
    def isFuiou: Boolean = value == Fuiou
    def isHelibao: Boolean = value == Helibao
  }
}

/**
 * 支付来源类型, 也就是支付方式
 */
final case class PaySource(value: String) extends AnyVal with StringMID

object PaySource {
  // 其他客户端
  val Other = PaySource("Other")
  // 微信
  val Weixin = PaySource("Weixin")
  // 支付宝
  val Alipay = PaySource("Alipay")
  // 云闪付
  val UnionPay = PaySource("UnionPay")
  // 京东支付
  val JD = PaySource("JD")
  // QQ 支付
  val QQ = PaySource("QQ")
}

// 支付方式
final case class PayWay(value: String) extends AnyVal with StringMID

object PayWay {
  // 付款码, 即被扫支付
  val Code = PayWay("Code")
  // JsApi(JavaScript) (嵌入到微信、支付宝等浏览器中, 本质上是一个 H5 页面，调用宿主浏览器的支付接口)
  val JsApi = PayWay("JsApi")
  // 原生支付, 调用支付工具直接支付
  val Native = PayWay("Native")
  // 小程序支付
  val MiniApp = PayWay("MiniApp")
  // SDK 支付 第三方 App 支付
  val SDK = PayWay("SDK")
  // 在第三方浏览器中支付
  val H5 = PayWay("H5")
}

// 支持的支付项目
final case class PaySupportItem(source: PaySource, way: PayWay)

// 包裹对象
final case class PaySupport(details: Seq[PaySupportItem] = Seq.empty) {

  // 增加项目
  def add(source: PaySource, way: PayWay): PaySupport =
    copy(details = (this.details :+ PaySupportItem(source, way)).distinct)

  // 是否包含支付来源和方式
  def contains(source: PaySource, way: PayWay): Boolean = details.exists(r => r.source == source && r.way == way)
}

// 支付状态
final case class PayState(value: String) extends AnyVal with StringMID

object PayState {
  // 初始化
  val Init = PayState("Init")
  // 支付中
  val Paying = PayState("Paying")
  // 已支付成功
  val Paid = PayState("Paid")
  // 支付错误
  val Error = PayState("Error")
  // 关闭了
  val Closed = PayState("Closed")
  // 退款中
  val Refunding = PayState("Refunding")
  // 退款成功, 如果退款失败，就返回 Paid 状态
  val Refund = PayState("Refund")
}
