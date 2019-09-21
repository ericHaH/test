package qianhub.libs.json.test

import qianhub.libs.model.Magic._
import qianhub.libs.model.Randoms
import qianhub.libs.utils

object TestSign {

  val LoopSize = 10000
  val ContentSize = 1024 * 50
  val Content = Randoms.genString(ContentSize)

  def testRSA() = {
    val KeySize = 1024
    //val Sign = "SHA1WithRSA"
    val Sign = "SHA256WithRSA"
    val (pub, pri) = utils.genKeyPair(KeySize)
    utils.timing {
      (0 until LoopSize).foreach { _ =>
        val sign = Content.bytes.signWithPrivate(pri, Sign)
        if (LoopSize == 1) {
          println(s"Public        : ${pub.getEncoded.base64}")
          println(s"Private       : ${pri.getEncoded.base64}")
          println(s"Sign256Private: ${sign.hex}")
          println(s"Sign256       : ${Content.bytes.sha256.hex}")
          println(s"Sign1         : ${Content.bytes.sha1.hex}")
        }
      }
    }(l => println(f"RSASign Total: $l millis AVG: ${l * 1.0 / LoopSize}%.4f"))

    utils.timing {
      val sign = Content.bytes.signWithPrivate(pri, Sign)
      (0 until LoopSize).foreach { _ =>
        val c = Content.bytes.verifyWithPublic(pub, sign, Sign)
        assert(c)
        if (LoopSize == 1) {
          println(s"Public        : ${pub.getEncoded.base64}")
          println(s"Private       : ${pri.getEncoded.base64}")
          println(s"Sign256Private: ${sign.hex}")
          println(s"Sign256       : ${Content.bytes.sha256.hex}")
          println(s"Sign1         : ${Content.bytes.sha1.hex}")
        }
      }
    }(l => println(f"RSAVerify Total: $l millis AVG: ${l * 1.0 / LoopSize}%.4f"))
  }

  def testHMAC() = {
    val salt = Randoms.genString(48)
    utils.timing {
      (0 until LoopSize).foreach { _ =>
        val t = Content.bytes.sha256(salt.bytes)
        if (LoopSize == 1) {
          println(s"Sign256: ${t.hex}")
        }
      }
    }(l => println(f"HMAC Total: $l millis AVG: ${l * 1.0 / LoopSize}%.4f"))
  }

  def main(args: Array[String]): Unit = {
    testRSA()
    testHMAC()
  }

}
