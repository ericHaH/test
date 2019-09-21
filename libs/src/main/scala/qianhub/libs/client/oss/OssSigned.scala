package qianhub.libs.client.oss

import java.io.File
import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.Locale

import qianhub.libs.model.Magic._

final case class OssSigned(
    verb: String,
    resource: String,
    ossQuery: Seq[String] = Seq.empty,
    ossHeaders: Map[String, String] = Map.empty,
    contentMD5: Option[String] = None,
    contentType: Option[String] = None,
    body: Option[String] = None) {

  val date: ZonedDateTime = ZonedDateTime.now()

  def toGMT: String = OssSigned.FMT.format(date)

  def fullResource: String = {
    if (ossQuery.isEmpty) resource
    else resource + "?" + ossQuery.mkString("&")
  }

  def headers(implicit oss: OssSetting): Map[String, String] = {
    var base = Map("Date" -> toGMT, "Authorization" -> sign)
    contentMD5.foreach(r => base += "Content-MD5" -> r)
    contentType.foreach(r => base += "Content-Type" -> r)
    base ++ ossHeaders
  }

  // refer: https://help.aliyun.com/document_detail/31951.html
  def sign(implicit oss: OssSetting): String = {
    val res = s"/${oss.bucket}$resource"
    val realRes = if (ossQuery.isEmpty) res else res + "?" + ossQuery.sorted.mkString("&")
    val h = ossHeaders.toList.sortBy(_._1).map { case (k, v) => s"$k:$v" }.mkString("\n")
    val realHeader = if (ossHeaders.isEmpty) "" else h + "\n"
    // signature签名
    val original = verb + "\n" + contentMD5.getOrElse("") + "\n" + contentType.getOrElse("") + "\n" + toGMT + "\n" + realHeader + realRes
    val target = original.bytes.sha1(oss.secret.value.bytes).base64
    // 返回值 authorization 授权
    "OSS " + oss.key.value + ":" + target
  }
}

object OssSigned {
  // 阿里云要求日期是 DD 格式，而系统自带的 RFC_1123_DATE_TIME 是 D 格式
  val FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).withZone(ZoneId.of("UTC"))

  // 上传文件
  def putObject(file: File, resource: String, md5: String, contentType: String): OssSigned = {
    new OssSigned("PUT", resource, contentMD5 = Some(md5), contentType = Some(contentType))
  }

  // 下载文件
  def getObject(resource: String): OssSigned = {
    new OssSigned("GET", resource)
  }

  // 删除文件
  def delObject(resource: String): OssSigned = {
    new OssSigned("DELETE", resource)
  }

  // 处理图片缩放
  def dealObject(resource: String, target: String)(implicit oss: OssSetting): OssSigned = {
    val o = target.bytes.base64
    val b = oss.bucket.bytes.base64
    val contentType = "text/plain; charset=UTF-8"
    val body = s"x-oss-process=image/resize,w_100|sys/saveas,o_$o,b_$b"
    new OssSigned("POST", resource, ossQuery = Seq("x-oss-process"), contentType = Some(contentType), body = Some(body))
  }
}
