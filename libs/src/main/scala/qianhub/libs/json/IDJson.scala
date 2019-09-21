package qianhub.libs.json

import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}

// 这种方法虽然繁琐一点，但性能是最好的，因为只需要生成一次对象
final class IDJson[I, T](f1: I => T, f2: T => Option[I])(implicit encoder: Encoder[I], decoder: Decoder[I])
    extends Format[T] {
  def apply(a: T): Json = f2(a).asJson
  def apply(c: HCursor): Decoder.Result[T] = c.as[I].map(f1)
}
