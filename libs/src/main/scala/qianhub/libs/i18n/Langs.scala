package qianhub.libs.i18n

import java.util.Locale

// 第一个是默认值
final case class Langs(langs: List[Lang]) {

  require(langs.nonEmpty, "langs must has one")

  val codes = langs.map(_.code)

  def default: Lang = langs.head

  // 找到最适合的, 如果没有就取第一个(默认值)
  def find(local: Locale): Lang = {
    langs.find(_.satisfies(local)).getOrElse(langs.head)
  }
}

object Langs {
  // 构建多语言
  def from(loader: ClassLoader, codes: List[String], prefix: String = "messages"): Langs = {
    val langs = codes.map { code =>
      Lang.from(loader, code, prefix)
    }
    new Langs(langs)
  }
}
