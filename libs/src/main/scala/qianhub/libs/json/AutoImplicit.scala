package qianhub.libs.json

import io.circe.generic.AutoDerivation
import io.circe.{Decoder, Encoder, HCursor}
import shapeless.{::, Generic, HNil, Lazy}

/**
 * 自动生成 value class 和普通对象的 Encoder & Decoder
 * 经过测试，与显式声明隐式对象相比， 使用该自动方式会有 10% 的性能损耗，在可以接受的范围内
 * 如果显式定义了隐式对象，则优先使用它.
 * 对于 ID 类，需要继承于 AnyVal，否则会自动转成包裹对象
 * 例如 final case class MyID(value: String) extends AnyVal
 * 对于对象 case class My(id: MyID)
 *   序列化时会转成 { "id": "abc" }
 *   如果不继承于 AnyVal 那么会变成 { "id": {"value": "abc"}}
 *   明显是错误的
 */
trait AutoImplicit extends AutoDerivation {

  implicit def decodeUnwrapped[A <: AnyVal, R](
      implicit
      gen: Lazy[Generic.Aux[A, R :: HNil]],
      decodeR: Decoder[R]): Decoder[A] =
    (c: HCursor) =>
      decodeR(c) match {
        case Right(unwrapped) => Right(gen.value.from(unwrapped :: HNil))
        case l @ Left(_)      => l.asInstanceOf[Decoder.Result[A]]
    }

  implicit def encodeUnwrapped[A <: AnyVal, R](
      implicit
      gen: Lazy[Generic.Aux[A, R :: HNil]],
      encodeR: Encoder[R]): Encoder[A] = (a: A) => encodeR(gen.value.to(a).head)
}
