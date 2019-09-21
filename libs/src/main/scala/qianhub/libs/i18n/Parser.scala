package qianhub.libs.i18n

import java.io._

import qianhub.libs.utils

import scala.io.Source
import scala.jdk.CollectionConverters._
import scala.util.Try

case class Message(key: String, pattern: String)

// 分解文件
final class Parser(loader: ClassLoader, name: String) {

  val MessageRe = """^([a-zA-Z0-9$_.-]+)\s*=\s*(.+)\s*$""".r

  def parse(): Map[String, Message] = {
    loader
      .getResources(name)
      .asScala
      .toList
      .reverse
      .flatMap { r =>
        val input = r.openStream()
        val ret = parse0(input).get
        input.close()
        ret
      }
      .map { case (k, v) => k -> Message(k, v) }
      .toMap
  }

  private def parse0(input: InputStream): Try[Map[String, String]] = Try {
    var map = Map.empty[String, String]
    utils.useSource(Source.fromInputStream(input, "utf-8")) { src =>
      src.getLines().foreach {
        case MessageRe(key, value) => map += key -> value
        case _                     =>
      }
      map
    }
  }
}
