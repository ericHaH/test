package qianhub.libs.utils

import java.io.File
import java.nio.file._
import java.security._

import qianhub.libs.model.Magic._

import scala.util.Try

/**
 * 安全有关函数
 */
trait SafeUtils {

  // 生成公钥私钥, 指定密匙长度（取值范围：512～2048）, 默认 1024 位
  def genKeyPair(in: Int = 1024): (PublicKey, PrivateKey) = {
    //生成证书, 创建‘密匙对’生成器
    val kpg = KeyPairGenerator.getInstance("RSA")
    //指定密匙长度（取值范围：512～2048）
    kpg.initialize(in)
    //生成‘密匙对’，其中包含着一个公匙和一个私匙的信息
    val kp = kpg.genKeyPair()
    //获得公匙和私钥
    (kp.getPublic(), kp.getPrivate())
  }

  // AES 通用加解密，加密成 HEX 编码。需要成对调用，生成 hex，解密时也是用 hex， 返回加密后的 hex
  def encrypt(password: String, value: String): String = value.bytes.encryptWithAES(password.bytes.aesKey).hex
  // 解密， value 是 hex 编码的
  def decrypt(password: String, valueHex: String): String = valueHex.hex.decryptWithAES(password.bytes.aesKey).string

  // 用 sha1 编码成 hex 格式
  def sha1Hex(text: String): String = text.bytes.sha1.hex
  // 禁止修改下面的函数
  def sha1Hex(code: String, salt: String): String = sha1Hex(s"code=$code;salt=$salt")
  // md5 编码
  def md5Hex(file: File): Try[String] = hashFile(file, "MD5").map(_.hex)
  // 获取文件 sha1 编码
  def sha1Hex(file: File): Try[String] = hashFile(file, "SHA-1").map(_.hex)
  // sha256 编码
  def sha256Hex(file: File): Try[String] = hashFile(file, "SHA-256").map(_.hex)

  def hashFile(file: File, algorithm: String): Try[Array[Byte]] = Try {
    val md = MessageDigest.getInstance(algorithm)
    val is = Files.newInputStream(file.toPath)
    val dis = new DigestInputStream(is, md)
    val buffer = new Array[Byte](8 * 1024)
    try {
      while (dis.read(buffer) >= 0) {}
    } finally {
      is.close()
    }
    md.digest()
  }
}
