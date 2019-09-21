package qianhub.libs.json

import io.circe._
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedAsObjectEncoder
import io.circe.generic.semiauto._
import io.circe.parser.{parse => cparse}
import io.circe.syntax._
import qianhub.libs.model.ApiErrors
import shapeless.Lazy

import scala.util.{Failure, Success, Try}

// 兼容 play-json 调用方式, 由于 Json 已用，使用 Jsons 代替
object Jsons {

  val SlimPrinter = Printer.noSpaces.copy(dropNullValues = true)
  val FatPrinter = Printer.spaces2.copy(dropNullValues = true)

  def Null: Json = Json.Null
  def True: Json = Json.True
  def False: Json = Json.False

  // 定义 Json 的方法
  def id[I, T](f1: I => T, f2: T => Option[I])(implicit encoder: Encoder[I], decoder: Decoder[I]): IDJson[I, T] =
    new IDJson[I, T](f1, f2)

  def format[T](implicit encode: Lazy[DerivedAsObjectEncoder[T]], decode: Lazy[DerivedDecoder[T]]): Format[T] =
    Format[T]

  def reads[T](implicit decode: Lazy[DerivedDecoder[T]]): Decoder[T] = deriveDecoder[T]

  def writes[T](implicit encode: Lazy[DerivedAsObjectEncoder[T]]): Encoder[T] = deriveEncoder[T]

  // 转成 Json 对象
  def toJson[T](data: T)(implicit encode: Encoder[T]): Json = data.asJson

  // 从 Json 对象转成目标对象
  def from[T](json: Json)(implicit decode: Decoder[T]): Try[T] =
    json.as[T].fold(ex => Failure(ApiErrors.InvalidJson.extra(ex)), r => Success(r))

  // 从字符串转成目标对象
  def from[T](input: String)(implicit d: Decoder[T]): Try[T] = parse(input).flatMap(r => from(r)(d))

  // 从字符串转成 Json
  def parse(input: String): Try[Json] =
    cparse(input).fold(ex => Failure(ApiErrors.InvalidJson.extra(ex)), r => Success(r))

  // 输出字符串
  def stringify(json: Json): String = SlimPrinter.print(json)

  // 将对象输出成字符串
  def stringify[T](obj: T)(implicit encode: Encoder[T]): String = SlimPrinter.print(toJson(obj))
}
