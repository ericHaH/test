package qianhub.libs.model

final case class StateID(value: Int) extends AnyVal with IntMID

object StateID {
  // 记录已经被删除(但还未从数据库中删除), 作废
  val Remove = StateID(0)
  // 正常
  val Enable = StateID(1)
  // 停用
  val Stop = StateID(2)
  // 确认中(排队中、运行中)
  val ACKing = StateID(3)
  // 草稿
  val Draft = StateID(5)
  // 冻结
  val Frozen = StateID(6)
  // 失败
  val Fail = StateID(7)
  // 成功
  val Success = StateID(8)
  // 系统对象, 禁止删除
  val System = StateID(9)
  // 未支付
  val NotCashier = StateID(100)
  // 支付
  val Cashier = StateID(102)
  // 退款
  val Refund = StateID(103)
}
