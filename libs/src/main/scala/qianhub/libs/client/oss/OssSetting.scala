package qianhub.libs.client.oss

import com.typesafe.config.Config
import qianhub.libs.model.{AppSecret, KeyID}

import scala.util.Try

final case class OssSetting(
    key: KeyID, // 签名标识
    secret: AppSecret, // 密码
    endpoint: String, // 访问地址
    bucket: String, // 仓库名称
    website: String // 外部网站地址
) {
  def apiUrl: String = s"https://$bucket.$endpoint"

  // ossFile 是绝对路径，必须以 / 开头
  def toUrl(ossFile: String): String = website + ossFile
}

object OssSetting {

  def from(conf: Config): Try[OssSetting] = Try {
    val key = KeyID(conf.getString("keyid"))
    val secret = AppSecret(conf.getString("secret"))
    val endpoint = conf.getString("endpoint")
    val bucket = conf.getString("bucket")
    val website = conf.getString("website")
    OssSetting(key, secret, endpoint, bucket, website)
  }
}
