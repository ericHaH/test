package qianhub.libs.model

/**
 * 强类型 ID 分为以下几种类型:
 * Short, Int, Long, Double, String
 * 说明: 不用再显示指定 Slick 的字段类型，而是自动解析到 Slick 的字段类型
 */
sealed trait MID[T] extends Any /* with slick.lifted.MappedTo[T] */ {
  def value: T
}

// Scala 处理泛型的代码比较繁琐, 这里简化处理
trait ShortMID extends Any with MID[Short]
trait IntMID extends Any with MID[Int]
trait LongMID extends Any with MID[Long]
trait DoubleMID extends Any with MID[Double]
trait StringMID extends Any with MID[String]
