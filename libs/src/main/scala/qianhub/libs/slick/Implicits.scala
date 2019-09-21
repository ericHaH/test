package qianhub.libs.slick

import java.security.{PrivateKey, PublicKey}

import qianhub.libs.model.Magic._
import qianhub.libs.model._
import shapeless.{::, Generic, HNil, Lazy}

import scala.reflect.ClassTag

/**
 * 隐式转换
 */
trait Implicits { self: SlickTrait =>
  import profile.api._

  // 原有类型  =》数据库类型的转换
  implicit final val PrivateKeyMapper =
    MappedColumnType.base[PrivateKey, String](x => x.getEncoded.base64, x => x.base64.privateKey)
  implicit final val PublicKeyMapper =
    MappedColumnType.base[PublicKey, String](x => x.getEncoded.base64, x => x.base64.publicKey)
  // 用于强类型 ID 映射成 Slick 的字段。为了提高运行和编译性能, 需要为每个 ID 显式指定 Mapper(采用泛型方法获取 Mapper 存在严重的性能隐患)
  final def IDMapper[T, U: BaseColumnType](f1: U => T, f2: T => Option[U])(
      implicit c: ClassTag[T]): BaseColumnType[T] = {
    MappedColumnType.base[T, U](to => f2(to).get, from => f1(from))
  }

  // 自动将 value class 转成 Slick 的字段类型. T 是 value class，U 是背后的 class
  // IDEA 比较傻，不认识这个隐式转换，会提示错误，但 SBT 编译却没有问题，所以这里使用具体类，缩小隐式转换的范围
  final def SlickValueClass[T, U](
      implicit gen: Lazy[Generic.Aux[T, U :: HNil]],
      base: BaseColumnType[U],
      c: ClassTag[T]): BaseColumnType[T] = {
    MappedColumnType.base[T, U](t => gen.value.to(t).head, u => gen.value.from(u :: HNil))
  }

  implicit final def SlickShort[T <: ShortMID](
      implicit gen: Lazy[Generic.Aux[T, Short :: HNil]],
      c: ClassTag[T]): BaseColumnType[T] = SlickValueClass[T, Short]

  implicit final def SlickInt[T <: IntMID](
      implicit gen: Lazy[Generic.Aux[T, Int :: HNil]],
      c: ClassTag[T]): BaseColumnType[T] = SlickValueClass[T, Int]

  implicit final def SlickLong[T <: LongMID](
      implicit gen: Lazy[Generic.Aux[T, Long :: HNil]],
      c: ClassTag[T]): BaseColumnType[T] = SlickValueClass[T, Long]

  implicit final def SlickDouble[T <: DoubleMID](
      implicit gen: Lazy[Generic.Aux[T, Double :: HNil]],
      c: ClassTag[T]): BaseColumnType[T] = SlickValueClass[T, Double]

  implicit final def SlickString[T <: StringMID](
      implicit gen: Lazy[Generic.Aux[T, String :: HNil]],
      c: ClassTag[T]): BaseColumnType[T] = SlickValueClass[T, String]
}
