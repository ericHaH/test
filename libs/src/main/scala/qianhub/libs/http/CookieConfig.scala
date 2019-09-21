package qianhub.libs.http

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

import com.typesafe.config.Config
import qianhub.libs.model.KeyID

import scala.concurrent.duration._
import scala.util.Try

// cookie 配置, 在 Cookie 中的名字和签名用的 Key
final case class CookieConfig(name: String, key: KeyID, timeout: FiniteDuration) {

  // 获取过期时间
  def expired: ZonedDateTime = ZonedDateTime.now().plusMinutes(timeout.toMinutes)
}

object CookieConfig {
  // 从配置文件中加载
  def from(cookieConf: Config): Try[CookieConfig] = Try {
    val name = cookieConf.getString("name")
    val key = cookieConf.getString("key")
    val timeout = cookieConf.getDuration("timeout")
    new CookieConfig(name, KeyID(key), FiniteDuration(timeout.toMillis, TimeUnit.MILLISECONDS))
  }
}
