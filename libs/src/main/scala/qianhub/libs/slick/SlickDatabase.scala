package qianhub.libs.slick

import java.io.Closeable

import com.typesafe.config.Config

import scala.concurrent._

/**
 * 封装 Slick 3.x 数据库
 */
trait SlickDatabase extends SlickProfileTrait with Closeable {
  import profile.api._

  final lazy val Api = profile.api

  // 执行上下文
  def ec: ExecutionContext

  def underlying: Database

  /** 没有事务运行 */
  final def run[T](a: DBIOAction[T, NoStream, Effect.All]): Future[T] = underlying.run(a)

  /** 携带事务运行 */
  final def runt[T](a: DBIOAction[T, NoStream, Effect.All]): Future[T] = underlying.run(a.transactionally)

  /** 关闭资源 */
  final def close(): Unit = underlying.close()
}

object SlickDatabase {
  import slick.jdbc._

  // h2 数据库
  def h2(path: String, config: Config)(pool: ExecutionContext): SlickDatabase = {
    new SlickDatabase {
      type SlickProfile = H2Profile.type
      val ec = pool
      val profile = H2Profile
      val underlying = H2Profile.api.Database.forConfig(path, config)
    }
  }

  // mysql 数据库
  def mysql(path: String, config: Config)(pool: ExecutionContext): SlickDatabase = {
    new SlickDatabase {
      type SlickProfile = MySQLProfile.type
      val ec = pool
      val profile = MySQLProfile
      val underlying = MySQLProfile.api.Database.forConfig(path, config)
    }
  }

  // postgres 数据库
  def postgres(path: String, config: Config)(pool: ExecutionContext): SlickDatabase = {
    new SlickDatabase {
      type SlickProfile = PgProfile.type
      val ec = pool
      val profile = PgProfile
      val underlying = PgProfile.api.Database.forConfig(path, config)
    }
  }
}
