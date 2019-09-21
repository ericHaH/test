package qianhub.libs.model

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import java.security._
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec._
import java.time._
import java.time.format.DateTimeFormatter
import java.util.zip.{Deflater, GZIPInputStream, GZIPOutputStream, Inflater}

import javax.crypto._
import javax.crypto.spec._
import javax.security.cert.X509Certificate
import org.apache.commons.codec.binary.{Base64, Hex}

import scala.annotation.tailrec
import scala.language.implicitConversions

/**
 * 时间相关
 */
object Magic {
  import DateFormat._
  import Magics._

  object DateFormat {
    val DateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
    // 作为文件名保存的
    val FileDateFmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault())
    val CompactFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault())
    val DayFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    val TimeFmt = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
    val TimeFmt2 = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
    val HJHDayFmt = DateTimeFormatter.ofPattern("MM-dd-yyyy").withZone(ZoneId.systemDefault())
  }

  object Magics {
    // 62进制对应关系，禁止修改该值 (TODO: 从配置文件获取, 但要保证第一个数字为 E)
    val all = "EeXsx84n9NjyTYtJuUfFAa5mMIivG61VqQLbgBlPp02CHcWh7rKwR3kODZozSd"
    val nums = "0123456789"
    val base = all.length
    val char2num = all.map(i => (i, all.indexOf(i))).toMap
    val num2char = char2num.map(_.swap)

    // AES 密码需要16位，因此需要填充
    def padding(bytes: Array[Byte], size: Int): Array[Byte] = {
      if (bytes.length >= size)
        bytes.slice(0, size)
      else
        bytes ++ Array.fill(size - bytes.length)(0.toByte)
    }
  }

  implicit class RichZonedDateTime(val date: ZonedDateTime) extends AnyVal with Ordered[ZonedDateTime] {
    def compare(x: ZonedDateTime) = date.compareTo(x)
    def simple: String = date.format(DateFmt)
    // 兼容
    // 紧凑型
    def compact: String = date.format(FileDateFmt)
    def day: String = date.format(DayFmt)
    def time: String = date.format(TimeFmt)
    // 黄记煌要求的格式
    def hjh: String = date.format(HJHDayFmt)
  }

  implicit class RichLocalTime(val date: LocalTime) extends AnyVal with Ordered[LocalTime] {
    def compare(x: LocalTime): Int = date.compareTo(x)
    def simple: String = date.format(TimeFmt)
    // 兼容
    def time: String = date.format(TimeFmt)
  }

  implicit class RichLocalDate(val date: LocalDate) extends AnyVal with Ordered[LocalDate] {
    def compare(x: LocalDate): Int = date.compareTo(x)
    def simple: String = date.format(DayFmt)
    // 兼容
    def day: String = date.format(DayFmt)
    // 黄记煌要求的格式
    def hjh: String = date.format(HJHDayFmt)
  }

  implicit class RichString2(val string: String) extends AnyVal {

    /** Symbol */
    def s = Symbol(string)

    /** 星化 */
    def star: String = {
      def withStar(pre: Int, post: Int) = string.take(pre) + "*" * (string.length - pre - post) + string.takeRight(post)
      string.length match {
        case i if i > 10 => withStar(3, 4)
        case i if i > 7  => withStar(2, 2)
        case i if i > 4  => withStar(1, 1)
        case _           => withStar(0, 0)
      }
    }

    /** 是否为 email */
    def isEmail: Boolean = string.contains("@")

    /** UTF-8 Bytes */
    def bytes: Array[Byte] = bytes("UTF-8")
    def bytes(encode: String): Array[Byte] = string.getBytes(encode)

    /** hex */
    def hex: Array[Byte] = Hex.decodeHex(string.toCharArray)

    /** Base64 */
    def base64: Array[Byte] = Base64.decodeBase64(string)

    // 解码62进制成 Long (目前只处理正数) TODO: 增加负数处理机制
    def base62: Long = string.foldLeft(0L)((total, i) => total * Magics.base + Magics.char2num.getOrElse(i, 0))

    // 转成 ZonedDateTime
    def datetime: ZonedDateTime =
      if (string.exists(_ == ':')) ZonedDateTime.parse(string, DateFmt) else ZonedDateTime.parse(string, DayFmt)

    // 转成 LocalTime
    def time: LocalTime =
      if (string.count(_ == ':') == 2) LocalTime.parse(string, TimeFmt) else LocalTime.parse(string, TimeFmt2)

    // 转成 LocalDate
    def date: LocalDate =
      if (string.exists(_ == ':')) LocalDate.parse(string, DateFmt) else LocalDate.parse(string, DayFmt)

    // encode & decode url
    def encodeUrl: String = encodeUrl("UTF-8")
    def encodeUrl(encode: String): String = java.net.URLEncoder.encode(string, encode)

    def decodeUrl: String = decodeUrl("UTF-8")
    def decodeUrl(encode: String): String = java.net.URLDecoder.decode(string, encode)

    // quote
    def q: String = s""""$string""""

    // option
    def opt: Option[String] = if (string.isEmpty) None else Some(string)

    // before
    def before(s: String): Option[String] = {
      string.indexOf(s) match {
        case i if i >= 0 => Some(string.take(i))
        case _           => None
      }
    }

    def dropRightWhile(p: Char => Boolean): String = {
      string.length match {
        case 0 => string
        case size =>
          var found = size - 1
          while (found >= 0 && p(string(found))) {
            found -= 1
          }
          string.dropRight(size - found - 1)
      }
    }
  }

  implicit class RichString3(val string: Option[String]) extends AnyVal {
    def real: Option[String] = if (string.nonEmpty && string.get.nonEmpty) string else None
  }

  implicit class RichArray(val bytes: Array[Byte]) extends AnyVal {
    def string(encode: String): String = new String(bytes, encode)

    /** String */
    def string: String = string("UTF-8")

    /** 转成 Hex */
    def hex: String = new String(Hex.encodeHex(bytes))

    /** 转成 Base64 */
    def base64: String = new String(Base64.encodeBase64(bytes), "UTF-8")

    /** 转成 MD5 */
    def md5: Array[Byte] = hash("MD5")

    /** 转成 sh1 */
    def sha1: Array[Byte] = hash("SHA-1")

    // SHA 256 签名
    def sha256: Array[Byte] = hash("SHA-256")

    def hash(algorithm: String): Array[Byte] = {
      val digest = MessageDigest.getInstance(algorithm)
      digest.update(bytes)
      digest.digest()
    }

    // 使用 SHA1 编码
    def sha1(key: Array[Byte]): Array[Byte] = hmac(key, "HmacSHA1")

    // HmacSHA256 签名, key 是加密盐
    def sha256(key: Array[Byte]): Array[Byte] = hmac(key, "HmacSHA256")

    // hmac编码
    def hmac(key: Array[Byte], algorithm: String): Array[Byte] = {
      val mac = Mac.getInstance(algorithm)
      val spec = new SecretKeySpec(key, algorithm)
      mac.init(spec)
      mac.doFinal(bytes)
    }

    // 转成公钥
    def publicKey: PublicKey = {
      // 构造X  509EncodedKeySpec 对象
      val spec = new X509EncodedKeySpec(bytes)
      // RSA对称加密算法
      val kf = KeyFactory.getInstance("RSA")
      // 取公钥匙对象
      kf.generatePublic(spec)
    }

    // 转成私钥
    def privateKey: PrivateKey = {
      val spec = new PKCS8EncodedKeySpec(bytes)
      val kf = KeyFactory.getInstance("RSA")
      kf.generatePrivate(spec)
    }

    // cert
    def certKey: javax.security.cert.Certificate = {
      X509Certificate.getInstance(bytes)
    }

    // 转成 AES 私钥
    def aesKey: SecretKey = new SecretKeySpec(padding(bytes, 16), "AES")
    // 转成 Sign 用的
    def signKey: SecretKey = new SecretKeySpec(bytes, "HmacSHA1")
    // DES
    def desKey: SecretKey = new SecretKeySpec(padding(bytes, 24), "DESede")

    // 使用 SecretKey 对 plain 进行签名
    def signWithSecret(signKey: SecretKey, algorithm: String = "HmacSHA1"): Array[Byte] = {
      val mac = Mac.getInstance(algorithm)
      mac.init(signKey)
      mac.doFinal(bytes)
    }

    // 用私钥签名
    def signWithPrivate(key: PrivateKey, algorithm: String = "SHA1WithRSA"): Array[Byte] = {
      // 用私钥对信息生成数字签名
      val signature = Signature.getInstance(algorithm)
      signature.initSign(key)
      signature.update(bytes)
      signature.sign()
    }

    // 用公钥验证
    def verifyWithPublic(key: PublicKey, signed: Array[Byte], algorithm: String = "SHA1WithRSA"): Boolean = {
      val checker = Signature.getInstance(algorithm)
      checker.initVerify(key)
      checker.update(bytes)
      // 验证签名是否正常
      checker.verify(signed)
    }

    // 用公钥加密
    def encryptWithPublic(key: PublicKey): Array[Byte] = {
      //获得一个RSA的Cipher类，使用公鈅加密
      val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
      cipher.init(Cipher.ENCRYPT_MODE, key)
      cipher.doFinal(bytes)
    }

    // 用私钥解密
    def decryptWithPrivate(key: PrivateKey): Array[Byte] = {
      //获得一个私鈅加密类 Cipher，ECB 是加密方式，PKCS5Padding 是填充方法
      val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
      cipher.init(Cipher.DECRYPT_MODE, key)
      cipher.doFinal(bytes)
    }

    // 使用随机 AES Key 密码加密，返回随机 AES 证书密钥、加密后的内容
    def encryptWithAESRandom(length: Int = 128): (SecretKey, Array[Byte]) = {
      //通过 KeyGenerator 形成一个 key
      val keyGen = KeyGenerator.getInstance("AES")
      keyGen.init(length)
      val key = keyGen.generateKey()
      // 获得一个密码加密类 Cipher，ECB 是加密方式，PKCS5Padding 是填充方法
      val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
      // 使用密钥加密
      cipher.init(Cipher.ENCRYPT_MODE, key)
      val cipherText = cipher.doFinal(bytes)
      (key, cipherText)
    }

    // 使用随机 AES Key 密码加密，返回加密后的内容
    def encryptWithAESRandomByPassword(password: Array[Byte], length: Int = 128): Array[Byte] = {
      // 通过 KeyGenerator 形成一个 key
      val keyGen = KeyGenerator.getInstance("AES")
      val random = SecureRandom.getInstance("SHA1PRNG")
      random.setSeed(password)
      keyGen.init(length, random)
      val key = keyGen.generateKey()
      val spec = new SecretKeySpec(key.getEncoded, "AES")
      // 获得一个密码加密类 Cipher，ECB 是加密方式，PKCS5Padding 是填充方法
      val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
      // 使用密钥加密
      cipher.init(Cipher.ENCRYPT_MODE, spec)
      cipher.doFinal(bytes)
    }

    // 使用随机 AES Key 密码加密，返回加密后的内容
    def decryptWithAESRandomByPassword(password: Array[Byte], length: Int = 128): Array[Byte] = {
      // 通过 KeyGenerator 形成一个 key
      val keyGen = KeyGenerator.getInstance("AES")
      val random = SecureRandom.getInstance("SHA1PRNG")
      random.setSeed(password)
      keyGen.init(length, random)
      val key = keyGen.generateKey()
      val spec = new SecretKeySpec(key.getEncoded, "AES")
      // 获得一个密码加密类 Cipher，ECB 是加密方式，PKCS5Padding 是填充方法
      val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
      // 使用密钥加密
      cipher.init(Cipher.DECRYPT_MODE, spec)
      cipher.doFinal(bytes)
    }

    // 使用指定 AES 密钥加密
    def encryptWithAES(aesKey: SecretKey): Array[Byte] = {
      val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
      cipher.init(Cipher.ENCRYPT_MODE, aesKey)
      cipher.doFinal(bytes)
    }

    // 使用 AES Key 解密
    def decryptWithAES(aesKey: SecretKey): Array[Byte] = {
      val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
      //使用密钥解密
      cipher.init(Cipher.DECRYPT_MODE, aesKey)
      cipher.doFinal(bytes)
    }

    // 使用指定 AES & IV 密钥加密
    def encryptWithAES(aesKey: SecretKey, iv: Array[Byte]): Array[Byte] = {
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      val ivSpec = new IvParameterSpec(iv)
      cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec)
      cipher.doFinal(bytes)
    }

    // 使用 AES Key & IV 解密
    def decryptWithAES(aesKey: SecretKey, iv: Array[Byte]): Array[Byte] = {
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      val ivSpec = new IvParameterSpec(iv)
      //使用密钥解密
      cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec)
      cipher.doFinal(bytes)
    }

    // 解密 AES GCM
    def decryptWithAESGcm(aesKey: SecretKey, iv: Array[Byte], aad: Array[Byte], length: Int = 128): Array[Byte] = {
      val cipher = Cipher.getInstance("AES/GCM/NoPadding")
      val spec = new GCMParameterSpec(length, iv)
      cipher.init(Cipher.DECRYPT_MODE, aesKey, spec)
      cipher.updateAAD(aad)
      cipher.doFinal(bytes)
    }

    // DES3 加密
    def encryptWithDES(desKey: SecretKey): Array[Byte] = {
      val cipher = Cipher.getInstance("DESede")
      cipher.init(Cipher.ENCRYPT_MODE, desKey)
      cipher.doFinal(bytes)
    }

    // DES3 解密
    def decryptWithDES(desKey: SecretKey): Array[Byte] = {
      val cipher = Cipher.getInstance("DESede")
      cipher.init(Cipher.DECRYPT_MODE, desKey)
      cipher.doFinal(bytes)
    }

    // zip 压缩 (zip 名称有冲突)
    def zipping: Array[Byte] = {
      val compress = new Deflater()
      compress.setInput(bytes)
      compress.finish()
      val bos = new ByteArrayOutputStream(bytes.length)
      val buffer = new Array[Byte](1024)
      while (!compress.finished) {
        val count = compress.deflate(buffer)
        bos.write(buffer, 0, count)
      }
      compress.end()
      bos.close()
      bos.toByteArray
    }

    // zip 解压
    def unzipping: Array[Byte] = {
      val decompress = new Inflater()
      decompress.setInput(bytes, 0, bytes.length)
      val bos = new ByteArrayOutputStream(bytes.length)
      val buffer = new Array[Byte](1024)
      while (!decompress.finished()) {
        val count = decompress.inflate(buffer)
        bos.write(buffer, 0, count)
      }
      decompress.end()
      bos.close()
      bos.toByteArray
    }

    // gzip 压缩
    def gzip: Array[Byte] = {
      val bos = new ByteArrayOutputStream()
      val out = new GZIPOutputStream(bos)
      out.write(bytes)
      out.close()
      bos.toByteArray
    }

    def gunzip: Array[Byte] = {
      val bis = new ByteArrayInputStream(bytes)
      val input = new GZIPInputStream(bis)
      val bos = new ByteArrayOutputStream(bytes.length)
      val buffer = new Array[Byte](1024)
      var len = input.read(buffer)
      while (len > 0) {
        bos.write(buffer, 0, len)
        len = input.read(buffer)
      }
      input.close()
      bos.toByteArray
    }
  }

  implicit class RichPrivateKey(val key: PrivateKey) extends AnyVal {
    // 获取公钥
    def publicKey: PublicKey = key match {
      case priKey: RSAPrivateCrtKey =>
        val spec = new RSAPublicKeySpec(priKey.getModulus, priKey.getPublicExponent)
        KeyFactory.getInstance("RSA").generatePublic(spec)
      case t => throw ApiErrors.WrongParam.extra(t.toString)
    }
  }

  implicit class RichLong(val value: Long) extends AnyVal {
    // 将 Long 编码成62进制(目前只处理正数) TODO: 增加负数处理机制
    def base62: String = {
      if (value == 0) return num2char(0).toString
      val str = new StringBuilder(12)
      var i = value
      while (i > 0) {
        str.append(num2char.getOrElse((i % base).toInt, 'E'))
        i = i / base
      }
      str.toString.reverse
    }
  }

  implicit class RichFile(val file: File) extends AnyVal {
    @tailrec
    final def subOf(parent: File): Boolean = {
      parent match {
        case null                => false
        case _ if file == parent => true
        case _                   => file.getParentFile.subOf(parent)
      }
    }
  }

  implicit class RichException(val throwable: Throwable) extends AnyVal {
    // 转换成基础异常(TODO 增加其他种类异常)
    def base: QianException = throwable match {
      case ex: QianException                         => ex
      case ex: java.net.SocketException              => ApiErrors.NetError.extra(ex)
      case ex: java.sql.SQLException                 => ApiErrors.DBError.extra(ex)
      case ex: java.util.concurrent.TimeoutException => ApiErrors.Timeout.extra(ex)
      case ex                                        => ApiErrors.SystemError.extra(ex)
    }
  }

  // 按照插入顺序分组排序
  implicit class GroupByOrdered[A](val t: Iterable[A]) extends AnyVal {
    def groupByOrdered[K](f: A => K): Seq[(K, Seq[A])] = {
      t.foldLeft(Vector.empty[(K, Seq[A])]) {
        case (b, v) =>
          val key = f(v)
          b.indexWhere(_._1 == key) match {
            case -1 => b :+ (key, Seq(v))
            case i  => b.updated(i, (key, b(i)._2 :+ v))
          }
      }
    }
  }

}
