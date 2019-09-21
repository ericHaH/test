package qianhub.libs

import scala.language.implicitConversions

/**
 * 该 package 出了 scala 外不依赖其他任何组件
 */
package object model {

  // 规整浮点数(处理小数位过多的问题, 例如 0.99999999999999 或者 1.0000000000001)
  def round(value: Double, base: Int = 100000000): Double = {
    if (value >= 0) roundPositive(value, base) else -roundPositive(-value, base)
  }

  // 比率时保留4为小数，因为要转成百分比
  def roundRatio(value: Double): Double = round(value, 10000)

  def round(value: Price): Price = Price(round(value.value, 100))

  // 正数
  private[this] def roundPositive(value: Double, base: Int): Double = {
    val l = value.toLong
    l + math.rint((value - l) * base) / base
  }
}
