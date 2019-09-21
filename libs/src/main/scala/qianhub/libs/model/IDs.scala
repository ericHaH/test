package qianhub.libs.model

/**
 * 常用的 ID
 */
final case class IntID(value: Int) extends AnyVal with IntMID
final case class LongID(value: Long) extends AnyVal with LongMID
final case class StringID(value: String) extends AnyVal with StringMID
final case class ID(value: Long) extends AnyVal with LongMID

// 品牌 & 门店
final case class BrandID(value: Long) extends AnyVal with LongMID
final case class ShopID(value: Long) extends AnyVal with LongMID

// 密钥
final case class KeyID(value: String) extends AnyVal with StringMID
// AppID 标识
final case class AppID(value: String) extends AnyVal with StringMID
// 用户标识
final case class OpenID(value: String) extends AnyVal with StringMID
// UnionID
final case class UnionID(value: String) extends AnyVal with StringMID
// 商户号
final case class MchID(value: String) extends AnyVal with StringMID
// 密码
final case class AppSecret(value: String) extends AnyVal with StringMID
// HttpAction
final case class HttpAction(value: String) extends AnyVal with StringMID
// RpcAction
final case class RpcAction(value: String) extends AnyVal with StringMID
// 开发者 ID
final case class DeveloperID(value: String) extends AnyVal with StringMID

// 这里先添加上
final case class SpuID(value: Long) extends AnyVal with LongMID
final case class SkuID(value: Long) extends AnyVal with LongMID
final case class ShelfID(value: Long) extends AnyVal with LongMID
final case class CategoryID(value: Long) extends AnyVal with LongMID
final case class DeliverID(value: Long) extends AnyVal with LongMID
final case class PackingID(value: Long) extends AnyVal with LongMID
final case class UnitTypeID(value: Long) extends AnyVal with LongMID
final case class SpecsID(value: Long) extends AnyVal with LongMID
final case class DisplayID(value: Long) extends AnyVal with LongMID
