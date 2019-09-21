package qianhub.libs.i18n

import java.text.MessageFormat
import java.util.Locale

import scala.jdk.CollectionConverters._

final case class Lang(locale: Locale, code: String, messages: Map[String, Message]) {

  val ranges = Seq(new Locale.LanguageRange(code)).asJava

  def i(key: String, args: Any*): String = {
    messages.get(key) match {
      case Some(i) =>
        val real = args.map(_.asInstanceOf[java.lang.Object]).toArray
        new MessageFormat(i.pattern, locale).format(real)
      case None => key
    }
  }

  def satisfies(target: Locale): Boolean = Locale.lookup(ranges, Seq(target).asJava) != null
}

object Lang {

  // 加载语言资源
  def from(loader: ClassLoader, code: String, prefix: String): Lang = {
    val local = new Locale.Builder().setLanguageTag(code).build()
    val name = s"$prefix.$code"
    val parser = new Parser(loader, name)
    val messages = parser.parse()
    new Lang(local, code, messages)
  }
}
