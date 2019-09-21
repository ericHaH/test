package qianhub.libs.model

// 单位: 元
final case class Price(value: Double) extends AnyVal with DoubleMID {
  // +
  //def +(other: Price): Price = Price(value + other.value)
  def +(other: Int): Price = Price(value + other)
  def +(other: Long): Price = Price(value + other)
  def +(other: Double): Price = Price(value + other)
  // -
  //def -(other: Price): Price = Price(value - other.value)
  def -(other: Int): Price = Price(value - other)
  def -(other: Long): Price = Price(value - other)
  def -(other: Double): Price = Price(value - other)
  // *
  def *(count: Int): Price = Price(value * count)
  def *(count: Double): Price = Price(value * count)

  def cent: Cent = Cent(math.round(value * 100))
  def neg: Price = Price(-value)
  def r: Price = round(this) // 保留2位小数
  def abs: Price = if (value >= 0) this else Price(-value)
  def isEmpty = value < 0.0001 && value > -0.0001 // double 要比较大小
  def nonEmpty = !isEmpty
}

object Price {
  import scala.language.implicitConversions

  val Z = Price(0)

  implicit def underlying(price: Price): Double = price.value

  // 提供运算(元)
  implicit object PriceNumeric extends Numeric[Price] {
    def compare(x: Price, y: Price): Int = x.value.compare(y.value)
    def plus(x: Price, y: Price): Price = Price(x.value + y.value)
    def minus(x: Price, y: Price): Price = Price(x.value - y.value)
    def times(x: Price, y: Price): Price = Price(x.value * y.value)
    def negate(x: Price): Price = Price(-x.value)
    def fromInt(x: Int): Price = Price(x)
    def toInt(x: Price): Int = x.value.toInt
    def toLong(x: Price): Long = x.value.toLong
    def toFloat(x: Price): Float = x.value.toFloat
    def toDouble(x: Price): Double = x.value

    override def parseString(str: String) = None
  }
}
