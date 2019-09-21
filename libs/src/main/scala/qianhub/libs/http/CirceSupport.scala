package qianhub.libs.http

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._
import akka.util.ByteString
import io.circe._
import qianhub.libs.json.Jsons

// 支持 Circe 的 Json 序列化操作
trait CirceSupport {

  final val mediaTypes = List(MediaTypes.`application/json`)
  final val UnmarshallerContentTypes = mediaTypes.map(ContentTypeRange.apply)

  implicit final val jsonMarshaller: ToEntityMarshaller[Json] =
    Marshaller.oneOf(mediaTypes: _*) { mediaType =>
      Marshaller.withFixedContentType(ContentType(mediaType)) { json =>
        HttpEntity(mediaType, Jsons.stringify(json))
      }
    }

  implicit final def marshaller[A: Encoder]: ToEntityMarshaller[A] = jsonMarshaller.compose(Encoder[A].apply)

  // 将 Body 转成 Json
  implicit final val JsonUnmarshaller: FromEntityUnmarshaller[Json] = {
    Unmarshaller.byteStringUnmarshaller.forContentTypes(UnmarshallerContentTypes: _*).map {
      case ByteString.empty => throw Unmarshaller.NoContentException
      case data             => jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
    }
  }

  implicit final def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] = {
    def decode(json: Json): A = Decoder[A].decodeJson(json).fold(throw _, identity)
    JsonUnmarshaller.map(decode)
  }
}

object CirceSupport extends CirceSupport
