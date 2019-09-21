package qianhub.libs.json

import io.circe.Json
import qianhub.libs.json.Jsons._

package object test extends JsonTrait {
  implicit class RichJson(val json: Json) extends AnyVal {
    def slim: String = SlimPrinter.pretty(json)
    def fat: String = FatPrinter.pretty(json)
  }
}
