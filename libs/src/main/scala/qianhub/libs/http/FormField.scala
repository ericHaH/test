package qianhub.libs.http

import java.io.File

import akka.http.scaladsl.model._

final case class FormField(
    field: String, // 字段名称
    value: Option[String], // 字段对应的字符串(可能有值)
    fileName: Option[String], // 文件名称(可能有值)
    contentType: Option[ContentType], // 内容类型(当 fileName 不为 None 时)
    target: Option[File] // 若是保存的目标文件，后期填入
) {
  // 是普通值
  def isValue: Boolean = value.nonEmpty && fileName.isEmpty
  // 是文件
  def isFile: Boolean = !isValue
}

object FormField {

  val CT = ContentType(MediaTypes.`application/octet-stream`)

  // 从数组中构建
  def from(seq: Seq[(String, String)]): Seq[FormField] = seq.map { case (k, v) => FormField.fromString(k, v) }

  // 值
  def fromString(filed: String, value: String): FormField = new FormField(filed, Some(value), None, None, None)

  // 文件
  def fromFile(filed: String, fileName: String, contentType: ContentType = CT, target: Option[File] = None): FormField =
    new FormField(filed, None, Some(fileName), Some(contentType), target)
}
