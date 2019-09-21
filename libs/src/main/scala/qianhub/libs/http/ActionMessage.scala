package qianhub.libs.http

import qianhub.libs.model._

/**
 * 消息结构采用以下方案:
 * 规划如下(例子):
 * POST https://xxx.com/gateway?method=PayService.BeginPay&id=12345678abcdeuf&dev_id=1001&sign=abcd1234ddfd
 * 解释:
 * 1) method 是调用的服务方法
 * 2) id 是请求的标识, 方便定位
 * 3) dev_id 是开发者的标识
 * 4) sign 是用开发者标识对请求体(即 body)做签名后的 hex 值
 * 5) body 是任意内容，与服务有关，与协议无关
 * 6) 使用者可以根据需要定制 Message 内容，可以不拘泥于 ActionMessage
 */
trait ActionMessage {
  def id: TradeID // 请求的 ID
  def appId: AppID // 应用的 ID，用于标识调用者身份
  def method: HttpAction // 方法
  def body: String // 请求内容，UTF-8 格式, 服务可以将它反序列化成想要的格式
}
