package qianhub.libs.slick

import qianhub.libs.model.DBType
import slick.jdbc.{H2Profile, MySQLProfile, PostgresProfile}

// 支持 Slick3
trait SlickTrait extends SlickProfileTrait with Implicits with SqlTrait {
  // 数据库类型
  def dbType: DBType
}

// H2 数据库
trait H2Slick extends SlickTrait {
  type SlickProfile = H2Profile.type
  lazy val profile = H2Profile

  def dbType: DBType = DBType.H2
}

// MySQL
trait MySQLSlick extends SlickTrait {
  type SlickProfile = MySQLProfile.type
  lazy val profile = MySQLProfile

  def dbType: DBType = DBType.MySQL
}

// Postgres(default)
trait DefaultPostgresSlick extends SlickTrait {
  type SlickProfile = PostgresProfile.type
  lazy val profile = PostgresProfile

  val DATETIME_CREATE = "TIMESTAMP DEFAULT NOW()" // PG 当前时间

  def dbType: DBType = DBType.PostgreSQL
}

trait PostgresSlick extends SlickTrait {
  type SlickProfile = PgProfile.type
  lazy val profile = PgProfile

  def dbType: DBType = DBType.PostgreSQL

}
