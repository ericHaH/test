package qianhub.libs.json

import io.circe._
import qianhub.libs.model._

trait IDTrait {
  // 快捷访问
  implicit val BrandIDJsons = Jsons.id(BrandID.apply, BrandID.unapply)
  implicit val ShopIDJsons = Jsons.id(ShopID.apply, ShopID.unapply)
  // 所有 ID 的 Json 序列化
  implicit val IDJsons = Jsons.id(ID, ID.unapply)
  implicit val CustomerIDJson = Jsons.id(CustomerID.apply, CustomerID.unapply)
  implicit val UserIDJson = Jsons.id(UserID.apply, UserID.unapply)
  implicit val AccountIDJson = Jsons.id(AccountID.apply, AccountID.unapply)
  implicit object PriceJson extends Encoder[Price] with Decoder[Price] {
    override def apply(a: Price): Json = Json.fromDoubleOrNull(round(a.value, 100))
    override def apply(c: HCursor): Decoder.Result[Price] = c.as[Double].map(Price.apply)
  }
  implicit val IntIDJson = Jsons.id(IntID.apply, IntID.unapply)
  implicit val LongIDJson = Jsons.id(LongID.apply, LongID.unapply)
  implicit val StringIDJson = Jsons.id(StringID.apply, StringID.unapply)
  implicit val CentJson = Jsons.id(Cent.apply, Cent.unapply)
  implicit val RatioJson = Jsons.id(Ratio, Ratio.unapply)
  implicit val SNJson = Jsons.id(SN.apply, SN.unapply)
  implicit val SHAJson = Jsons.id(SHA.apply, SHA.unapply)
  implicit val TradeIDJson = Jsons.id(TradeID.apply, TradeID.unapply)
  implicit val StateIDJson = Jsons.id(StateID.apply, StateID.unapply)
  implicit val TokenJson = Jsons.id(Token.apply, Token.unapply)
  // 需要隐藏的字符串, 例如密码
  implicit object HiddenJsons extends Encoder[Hidden] with Decoder[Hidden] {
    override def apply(a: Hidden): Json = Json.fromString("***")
    override def apply(c: HCursor): Decoder.Result[Hidden] = {
      c.as[String].map(Hidden.apply)
    }
  }
  // bill
  implicit val BillIDJson = Jsons.id(BillID.apply, BillID.unapply)
  implicit val OrderIDJson = Jsons.id(OrderID.apply, OrderID.unapply)
  implicit val DetailIDJson = Jsons.id(DetailID.apply, DetailID.unapply)
  implicit val CRUDJson = Jsons.id(CRUD.apply, CRUD.unapply)
  // weixin
  implicit val AppIDJson = Jsons.id(AppID, AppID.unapply)
  implicit val KeyIDJson = Jsons.id(KeyID, KeyID.unapply)
  implicit val OpenIDJson = Jsons.id(OpenID, OpenID.unapply)
  implicit val MchIDJson = Jsons.id(MchID, MchID.unapply)
  implicit val AppSecretJson = Jsons.id(AppSecret, AppSecret.unapply)
  implicit val HttpActionJson = Jsons.id(HttpAction.apply, HttpAction.unapply)
  implicit val RpcActionJson = Jsons.id(RpcAction.apply, RpcAction.unapply)
  implicit val SignTypeJson = Jsons.id(SignType.apply, SignType.unapply)
  // SourceType
  implicit val SourceTypeFormat = Jsons.id(PaySource.apply, PaySource.unapply)
  implicit val QRCodeJson = Jsons.id(QRCode.apply, QRCode.unapply)
  implicit val ProviderIDJson = Jsons.id(ProviderID.apply, ProviderID.unapply)
  implicit val AgentIDJson = Jsons.id(AgentID.apply, AgentID.unapply)
  implicit val MerchantIDJson = Jsons.id(MerchantID.apply, MerchantID.unapply)
  implicit val LogIDJson = Jsons.id(LogID.apply, LogID.unapply)
  implicit val AuthCodeJson = Jsons.id(AuthCode.apply, AuthCode.unapply)
  implicit val DevIDJson = Jsons.id(DeveloperID.apply, DeveloperID.unapply)
  // Pay
  implicit val PayChannelJson = Jsons.id(PayChannel.apply, PayChannel.unapply)
  implicit val PayWayJson = Jsons.id(PayWay.apply, PayWay.unapply)
  // object
  implicit val ShaFileJson = Jsons.format[ShaFile]
  implicit val PaySupportJson = Jsons.format[PaySupportItem]
  implicit val PaySupportsJson = Jsons.format[PaySupport]
  implicit val PayStateJson = Jsons.id(PayState.apply, PayState.unapply)
  // 这里先添加上
  implicit val SpuIDJsons = Jsons.id(SpuID, SpuID.unapply)
  implicit val SkuIDJsons = Jsons.id(SkuID, SkuID.unapply)
  implicit val ShelfIDJsons = Jsons.id(ShelfID, ShelfID.unapply)
  implicit val CategofryIDJsons = Jsons.id(CategoryID, CategoryID.unapply)
  implicit val DeliverIDJsons = Jsons.id(DeliverID, DeliverID.unapply)
  implicit val PackingIDJsons = Jsons.id(PackingID, PackingID.unapply)
  implicit val UnitTypeIDJsons = Jsons.id(UnitTypeID, UnitTypeID.unapply)
  implicit val SpecsIDJsons = Jsons.id(SpecsID, SpecsID.unapply)
  implicit val DisplyaIDJsons = Jsons.id(DisplayID, DisplayID.unapply)
}
