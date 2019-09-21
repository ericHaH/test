package qianhub.libs.model

// 收银有关
final case class BillID(value: Long) extends AnyVal with LongMID
final case class OrderID(value: Long) extends AnyVal with LongMID
final case class DetailID(value: Long) extends AnyVal with LongMID
