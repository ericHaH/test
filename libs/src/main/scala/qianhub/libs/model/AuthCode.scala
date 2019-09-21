package qianhub.libs.model

import scala.util.{Failure, Success, Try}

/**
 * 验证码
 */
final case class AuthCode(value: String) extends AnyVal with StringMID {

  // 验证码是付款码时判断支付方式
  def toPaySource: Try[PaySource] = {
    value.take(2) match {
      // 支付宝: 付款码将由原来的28开头扩充到25-30开头，长度由原来的16-18位扩充到16-24位
      case "25" | "26" | "27" | "28" | "29" | "30" if value.length >= 16 => Success(PaySource.Alipay)
      // 微信: 以10、11、12、13、14、15开头的18位纯数字(为了兼容 >=18)
      case "10" | "11" | "12" | "13" | "14" | "15" if value.length >= 18 => Success(PaySource.Weixin)
      // 云闪付 62 开头的
      case "62" if value.length >= 18 => Success(PaySource.UnionPay)
      case _                          => Failure(ApiErrors.AuthCodeOfPayError.extra(value))
    }
  }

}
