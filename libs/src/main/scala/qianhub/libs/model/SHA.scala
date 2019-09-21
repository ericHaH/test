package qianhub.libs.model

import java.io.File

import org.apache.commons.io.FilenameUtils
import qianhub.libs.utils

import scala.util.Try

// SHA1+Ext 如 4056712d2e6db043a38b78c4ee2130c74bae7216.jpg
final case class SHA(value: String) extends AnyVal with StringMID {

  // 获取扩展名
  def ext: Option[String] = {
    val Max = value.length - 1
    value.lastIndexOf('.') match {
      case -1 | Max => None
      case i        => Some(value.substring(i + 1, value.length))
    }
  }

  // 默认2个字符前缀
  def prefix: String = prefix(2)

  // 指定前缀
  def prefix(size: Int): String = value.substring(0, size)

  def toFile(parent: File, size: Int = 2): File = new File(new File(parent, prefix(size)), value)

  // 映射成 URL 路径
  def toUrl(web: String, size: Int = 2): String = s"$web/${prefix(size)}/$value"

}

object SHA {
  // 从文件构建 SHA 对象
  def from(file: File): Try[SHA] = Try {
    val sha = utils.sha1Hex(file).get
    val ext = FilenameUtils.getExtension(file.getName).toLowerCase
    if (ext.isEmpty) SHA(sha) else SHA(sha + "." + ext)
  }
}

/**
 * 具备父目录和 SHA 文件的对象
 * 例如 ShaFile("/home/sanyi/satic/image", "2de92fa8d7ca99b5c6195724f4f64f266aabebbb.jpg")
 */
final case class ShaFile(parent: String, sha: SHA) {
  def toUrl: String = sha.toUrl(parent)
}
