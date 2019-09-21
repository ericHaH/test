package qianhub.libs.json

import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedAsObjectEncoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, HCursor, Json}
import shapeless.Lazy

trait Format[T] extends Encoder[T] with Decoder[T]

// 封装成方法调用
object Format {
  def apply[T](implicit encode: Lazy[DerivedAsObjectEncoder[T]], decode: Lazy[DerivedDecoder[T]]): Format[T] =
    new Format[T] {
      final val encoder = deriveEncoder[T]
      final val decoder = deriveDecoder[T]
      final def apply(a: T): Json = encoder(a)
      final def apply(c: HCursor): Decoder.Result[T] = decoder(c)
    }
}
