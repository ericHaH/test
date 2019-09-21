package qianhub.libs.model

final case class DBType(value: String) extends AnyVal with StringMID

object DBType {
  val H2 = DBType("H2")
  val MySQL = DBType("MySQL")
  val PostgreSQL = DBType("PostgreSQL")
}
