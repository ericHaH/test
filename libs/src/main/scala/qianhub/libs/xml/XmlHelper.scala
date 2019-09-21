package qianhub.libs.xml

import scala.xml.{Node, XML}

object XmlHelper {

  // Map 转成 XML 字符串
  def map2Xml(map: Map[String, String]): Node = {
    val xml = map.map { case (k, v) => s"<$k><![CDATA[$v]]></$k>" }.mkString("<xml>\n", "\n", "\n</xml>")
    XML.loadString(xml)
  }

  // XML 转成 Map
  def xml2Map(xml: Seq[Node]): Map[String, String] = {
    xml
      .map { node =>
        node.label -> node.text
      }
      .filter(_._2.trim.nonEmpty)
      .toMap
  }

  // XML 转成 Map，如果是空串，使用 None
  def xml2MapOpt(xml: Seq[Node]): Map[String, Option[String]] = {
    xml.map { node =>
      val text = node.text.trim
      if (text.isEmpty) node.label -> None else node.label -> Some(text)
    }.toMap
  }
}
