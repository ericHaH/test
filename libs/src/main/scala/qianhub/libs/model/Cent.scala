package qianhub.libs.model

// 单位: 分
final case class Cent(value: Long) extends AnyVal with LongMID {
  // +
  //def +(other: Cent): Cent = Cent(value + other.value)
  def +(other: Int): Cent = Cent(value + other)
  def +(other: Long): Cent = Cent(value + other)
  def +(other: Double): Cent = Cent(value + other.toLong)
  // -
  //def -(other: Cent): Cent = Cent(value - other.value)
  def -(other: Int): Cent = Cent(value - other)
  def -(other: Long): Cent = Cent(value - other)
  def -(other: Double): Cent = Cent(value - other.toLong)
  // *
  def *(count: Int): Cent = Cent(value * count)
  def *(count: Double): Cent = Cent((value * count).toLong)

  def price = Price(1.0 * value / 100)
  def neg: Cent = Cent(-value)
  def abs: Cent = if (value >= 0) this else Cent(-value)
  def isEmpty: Boolean = value == 0
  def nonEmpty: Boolean = value != 0
}

object Cent {
  import scala.language.implicitConversions

  val Z = Cent(0)

  implicit def underlying(cent: Cent): Long = cent.value

  // 提供运算(分)
  implicit object CentNumeric extends Numeric[Cent] {
    def compare(x: Cent, y: Cent): Int = x.value.compare(y.value)
    def plus(x: Cent, y: Cent): Cent = Cent(x.value + y.value)
    def minus(x: Cent, y: Cent): Cent = Cent(x.value - y.value)
    def times(x: Cent, y: Cent): Cent = Cent(x.value * y.value)
    def negate(x: Cent): Cent = Cent(-x.value)
    def fromInt(x: Int): Cent = Cent(x)
    def toInt(x: Cent): Int = x.value.toInt
    def toLong(x: Cent): Long = x.value
    def toFloat(x: Cent): Float = x.value.toFloat
    def toDouble(x: Cent): Double = x.value.toDouble

    override def parseString(str: String) = None
  }
}
