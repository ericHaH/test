package qianhub.libs.model

// 基本操作
final case class CRUD(value: String) extends AnyVal with StringMID

object CRUD {
  val Create = CRUD("Create")
  val Read = CRUD("Read")
  val Update = CRUD("Update")
  val Delete = CRUD("Delete")
}
