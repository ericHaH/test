package qianhub.libs.json

import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.{LocalDate, LocalDateTime, ZoneId, ZonedDateTime}

import io.circe._
import io.circe.syntax._

trait JsonTrait extends IDTrait with AutoImplicit {

  implicit object LocalDateTimeJson extends Encoder[LocalDateTime] with Decoder[LocalDateTime] {

    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    override def apply(a: LocalDateTime): Json = a.format(fmt).asJson

    override def apply(c: HCursor): Decoder.Result[LocalDateTime] = c.as[String].flatMap { s =>
      try Right(LocalDateTime.parse(s, fmt))
      catch {
        case _: DateTimeParseException => Left(DecodingFailure("LocalDateTime", c.history))
      }
    }
  }

  implicit object LocalDateJson extends Encoder[LocalDate] with Decoder[LocalDate] {

    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override def apply(a: LocalDate): Json = a.format(fmt).asJson

    override def apply(c: HCursor): Decoder.Result[LocalDate] = c.as[String].flatMap { s =>
      try Right(LocalDate.parse(s, fmt))
      catch {
        case _: DateTimeParseException => Left(DecodingFailure("LocalDate", c.history))
      }
    }
  }

  implicit object ZonedDateTimeJson extends Encoder[ZonedDateTime] with Decoder[ZonedDateTime] {

    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    override def apply(a: ZonedDateTime): Json = a.format(fmt).asJson

    override def apply(c: HCursor): Decoder.Result[ZonedDateTime] = c.as[String].flatMap { s =>
      try Right(ZonedDateTime.parse(s, fmt))
      catch {
        case _: DateTimeParseException => Left(DecodingFailure("ZonedDateTime", c.history))
      }
    }
  }

  implicit object StringDecoder extends Decoder[String] {
    override def apply(c: HCursor): Decoder.Result[String] = {
      if (c.value.isNumber) Right(c.value.asNumber.map(_.toString).get)
      else Right(c.value.asString.get)
    }
  }

  private def fixNumber[T](c: HCursor)(f1: String => T, f2: JsonNumber => T): Decoder.Result[T] = {
    try {
      if (c.value.isNumber) Right(c.value.asNumber.map(f2).get)
      else Right(f1(c.value.asString.get))
    } catch {
      case _: Exception => Left(DecodingFailure("Number", c.history))
    }
  }

  // Web 提交的数字可能包含了字符串
  implicit object LongDecoder extends Decoder[Long] {
    override def apply(c: HCursor): Decoder.Result[Long] = fixNumber(c)(_.toLong, _.toLong.get)
  }

  implicit object IntDecoder extends Decoder[Int] {
    override def apply(c: HCursor): Decoder.Result[Int] = fixNumber(c)(_.toInt, _.toInt.get)
  }

  implicit object ShortDecoder extends Decoder[Short] {
    override def apply(c: HCursor): Decoder.Result[Short] = fixNumber(c)(_.toShort, _.toShort.get)
  }

  implicit object BooleanDecoder extends Decoder[Boolean] {
    override def apply(c: HCursor): Decoder.Result[Boolean] = {
      try {
        if (c.value.isBoolean) Right(c.value.asBoolean.get)
        else if (c.value.isNumber) Right(c.value.asNumber.flatMap(_.toInt).contains(1))
        else {
          c.value.asString.map(_.toLowerCase) match {
            case Some("true") | Some("1")  => Right(true)
            case Some("false") | Some("0") => Right(false)
            case _                         => Right(false)
          }
        }
      } catch {
        case _: Exception => Left(DecodingFailure("Boolean", c.history))
      }
    }
  }

  implicit val QianExceptionFormat = new QianExceptionFormat()
  implicit val PagesFormat = new PagesFormat()
}
