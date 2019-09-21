package qianhub.libs.model

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * 数据处理
 */
object Randoms {
  import Magic.Magics._

  private[this] val SRandom = SecureRandom.getInstance("SHA1PRNG"); //new SecureRandom()
  private[this] val DateFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

  // 禁止修改下面函数
  def genToken: String = genString
  def genSalt: String = genString

  // 随机生成字符串(长12位)
  def genString: String = genString(12)
  // 随机生成指定长度的随机字符串
  def genString(size: Int): String =
    (0 until size).map { _ =>
      num2char(SRandom.nextInt(base))
    }.mkString

  // 时间错字符串
  def genDate: String = ZonedDateTime.now().format(DateFmt)

  // 随机生成 Long
  def genLong: Long = {
    val bytes = new Array[Byte](16)
    SRandom.nextBytes(bytes)
    ByteBuffer.wrap(bytes).getLong
  }

  // 随机生成数字字符串
  def genNumString(size: Int): String =
    (0 until size).map { _ =>
      nums(SRandom.nextInt(10))
    }.mkString

  // 随机生成无符号的 Long
  def genULong: Long = math.abs(genLong)

  def genInt: Int = {
    val bytes = new Array[Byte](8)
    SRandom.nextBytes(bytes)
    ByteBuffer.wrap(bytes).getInt
  }

  def genUInt: Int = math.abs(genInt)

  // 将 BigInt 编码成62进制(目前只处理正数, 内部使用暂不开放，若 Long 不能满足唯一性，可以使用 UUID)
  // TODO: 增加负数处理机制
  private[this] def encode62Big(num: BigInt): String = {
    if (num.toInt == 0) return "0"
    val str = new StringBuilder(24)
    var i = num
    while (i > 0) {
      str.append(num2char.getOrElse((i % base).toInt, '0'))
      i = i / base
    }
    str.toString.reverse
  }

  // 将 UUID 转换成62进制
  def encode62(uuid: UUID): String = {
    val uu = uuid.toString.split("-").mkString("")
    val num = uu.foldLeft(BigInt(0L))((total, i) => total * 16 + i - '0')
    encode62Big(num)
  }
}
