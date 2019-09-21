package qianhub.libs.model

// 系统级错误 10
sealed trait SystemErrors {
  // 系统错误
  def SystemError = QianException(1000, "System Error")
  // 数据库异常
  def DBError = QianException(1001, "Database Error")
  // 资源没有找到
  def NotFound = QianException(1004, "Resource Not Found")
  // 数据不对
  def WrongData = QianException(1005, "Wrong Data")
  // 禁止
  def Forbidden = QianException(1006, "Access Forbidden")
  // 无法删除，要填入无法删除的信息
  def NotRemoved = QianException(1007, "It Can't Remove")
  // 没有操作的权限
  def NoRight = QianException(1008, "Not Right")
  // 余额不足
  def Insufficient = QianException(1009, "Insufficient Balance")
  // 添加失败：名字相同
  def SameName = QianException(1010, "Same Name")
  // 添加失败: 工号相同
  def SameSN = QianException(1011, "Same SN")
  // 资源已存在
  def ResourceExist = QianException(1012, "Resource Exists")
  // 修改密码失败
  def ChangePasswordFailed = QianException(1013, "Wrong user name or old password")
  // 密码格式不对
  def WrongPasswordFormat = QianException(1014, "Wrong password format")
  // 没有数据
  def NoData = QianException(1015, "No Data")
  // 参数错误
  def WrongParam = QianException(1016, "Wrong Parameter")
  // 添加失败: rfid相同
  def SameRF = QianException(1017, "Same RfID")
  // 网络故障
  def NetError = QianException(1018, "Network Error")
  // 访问超时
  def Timeout = QianException(1019, "Timeout")
  // 删除失败：菜品已添加定价计划，无法删除
  def HasPricing = QianException(1020, "Goods Has Pricing")
  // 添加失败：类型重复
  def SameType = QianException(1021, "Same Type")
  // 删除失败：做法已添加定价计划，无法删除
  def GoodsMethodHasPricing = QianException(1022, "GoodsMethod Has Pricing")
  // 删除失败：规格已被使用，无法删除
  def UnitTypeHasUsing = QianException(1023, "UnitType Has using")
  // 菜品必须至少有一个规格
  def GoodsMustHasUnitType = QianException(1024, "Goods must has unitType")
  // 重复操作
  def RepeatAction = QianException(1025, "Repeat Action")
  // 添加规格失败： 菜品已存在相同规格， 无法再次添加
  def UnitTypeHasExsit = QianException(1026, "UnitType has exist")
  // 帐号已经注册
  def AccountHasExsit = QianException(1027, "account has exist")
  // 验证码错误
  def CodeError = QianException(1028, "code error")
  // 缓存已经失效
  def CacheExpired = QianException(1029, "cache expired")
  // 手机号不一致
  def MobileDiffer = QianException(1030, "mobile differ")
  // 没有登录
  def NoLogin = QianException(1031, "no log in")
  // 已经支付
  def YetCashier = QianException(1032, "yet cashier")
  // 已经过期
  def CashierExpire = QianException(1033, "cashier time expire")
  // 部署成功
  def DeployFailure = QianException(1034, "deploy failure")
  def NotCashier = QianException(1035, "not cashier")
  // 等待登录
  def WaitLogin = QianException(1036, "wait login")
  // 扫码登录失败
  def ScanLoginFailure = QianException(1037, "scan login failure")
  // 品牌已经存在
  def BrandExists = QianException(1038, "brand exists")
  // 门店已经存在
  def ShopExists = QianException(1039, "shop exists")
  // 试用门店需要续费
  def ShopTrailRenewals = QianException(1040, "shop trail renewals")
  // 停止续费
  def ForbidRenewals = QianException(1041, "shop forbid renewals")
  //
  def CodeRepetionWrongInput = QianException(1042, "wrong code repetion input")
  // 不支持的浏览器
  def BrowserNotSupport = QianException(1043, "Browser not support")
  // 不支持
  def NotSupport = QianException(1044, "Not Support")
  // 用户不存在或者密码错误
  def LoginError = QianException(1045, "Login Error")
  // 没有权限
  def NoPermission = QianException(1046, "No Permission")
}

// 请求异常 11
sealed trait RequestErrors {
  def InvalidForm = QianException(1101, "Form Validation Failed")
  // 缺少头部信息
  def MissingHeader = QianException(1102, "Missing API Header")
  // 非法头部
  def InvalidHeader = QianException(1103, "Invalid Header")
  // API 提交的对象校验失败
  def InvalidJson = QianException(1104, "Invalid Json")
  // 不是 JSON 对象
  def NoJson = QianException(1105, "Not A Json From POST Request")
  // 没有找到注册信息
  def PosNotFound = QianException(1106, "Pos Machine Is Not Found")
  // Salt 是空的
  def SaltIsEmpty = QianException(1107, "Salt Is Empty. Please Register.")
  // 不合法的 Token
  def InvalidToken = QianException(1108, "Invalid Token")
  // Json 对象没有指定的数据
  def NotBindingData = QianException(1109, "Json Object Has Not Binding Data")
  // Missing File
  def MissingFile = QianException(1110, "Missing File")
  // Too Big
  def TooBigFile = QianException(1111, "File Is Too Big")
  // UUID is wrong
  def WrongUUID = QianException(1112, "Wrong UUID")
  // App not found
  def AppNotFound = QianException(1113, "App Is Not Found")
  // 付款码格式错误
  def AuthCodeOfPayError = QianException(1114, "Auth Code Error")
}

// 时间异常 12
sealed trait TimeErrors {
  def NotComing = QianException(1201, "Begin Time Is Not Coming")
  def Expired = QianException(1202, "Expired")
  def NotInWeek = QianException(1203, "Wrong Week")
  def ServiceHourOverride = QianException(1204, "Service Hour is Override")
  def NotInServiceHour = QianException(1205, "Not In Service Hour")
}

// Shop 信息: 13
sealed trait ShopErrors {
  def InvalidShop = QianException(1301, "Invalid Shop")
  def QRUsed = QianException(1302, "QRCode was used")
  def QRExpired = QianException(1303, "QRCode was expired")
  def QRNotFound = QianException(1304, "QRCode was not found")
  def TableNotFound = QianException(1305, "Table not found")
  def CannotDeleteMasterPos = QianException(1306, "Cannot Delete Master PosMachine")
}

// Coupon 异常 16
trait CouponErrors {
  // 没有找到 Voucher
  def CouponNotFound = QianException(1601, "Coupon Not Found")
  def CouponUsed = QianException(1602, "Coupon Has Been Used")
  def CouponNoUseHere = QianException(1604, "Coupon No Use Here")
}

// 会员异常 17
trait MemberErrors {
  def LackOfBalance = QianException(1701, "The balance of the member is too less")
  def NoCashCard = QianException(1702, "The card is not a cash card")
  def MobileExists = QianException(1703, "Mobile Exists")
  def EmailExists = QianException(1704, "Email Exists")
  def CardExists = QianException(1705, "Card No. Exists")
  def RFIDExists = QianException(1706, "RFID Exists")
  def VerifyCodeExpired = QianException(1707, "Verify Code is expired")
  def YetVerified = QianException(1708, "It has been verified")
  def WrongVerifyCode = QianException(1709, "Wrong Verify Code")
  def MultiMemberTypes = QianException(1710, "Multi MemberTypes")
  def ProfileUpdated = QianException(1711, "Profile was updated")
  def MemberNotFound = QianException(1712, "Member Not Found")
  def NotAck = QianException(1713, "Not ACK")
  def UseChargeNotFound = QianException(1714, "UseCharge Not Found")
  def MultiMembers = QianException(1715, "Multi Members")
  def YetBoundCard = QianException(1716, "Yet Bound Card")
  def VerifyCodeNotFound = QianException(1717, "Verify Code Not Found")
  def MemberPasswordNotMatch = QianException(1718, "Member Password Not Match")
  def MemberPasswordMissing = QianException(1719, "Member Password Is Missing")
  def EmailOrMobileExists = QianException(1720, "Email Or Mobile Exists")
  def ChargeNotFound = QianException(1721, "Charge Not Found")
  def PromotionNotFound = QianException(1722, "Promotion Not Found")
}

// goods 异常 18
sealed trait GoodsErrors {
  def GoodsHasQuoted = QianException(1801, " The goods has been quoted by set")
}

// Voucher 异常 20
sealed trait VoucherErrors {
  // 没有找到 Voucher
  def VoucherNotFound = QianException(2001, "Voucher Not Found")
  def VoucherUsed = QianException(2002, "Voucher Has Been Used")
  def VoucherNotFoundForNoSN = QianException(2003, "Voucher Not Found for Non-SN")
  def VoucherNoUseHere = QianException(2004, "Voucher No Use Here")
  def VoucherTypeUsed = QianException(2005, "Voucher Type is used")
}

// 收银异常 21
sealed trait CashierErrors {
  def NoBill = QianException(2100, "No Cashier Bill")
  def RepeatCashier = QianException(2101, "Bill has been cashiered")
  def InvalidBillID = QianException(2102, "Invalid Bill ID")
  def TimeoutBill = QianException(2103, "Timeout Bill")
  def PosNotConnected = QianException(2104, "Pos has not been connected")
  def TimeoutPay = QianException(2105, "Timeout PayFinish")
}

// 微信错误, 22
sealed trait WeixinErrors {
  def ShortUrlFailed = QianException(2201, "Failed to short url")
  def BillNotFound = QianException(2202, " Bill not found")
  def NotSupportWeixin = QianException(2203, "Shop does not support weixin pay")
  def OpenIDNotFound = QianException(2204, "Not OpenID")
  def UserNotAuth = QianException(2205, "User Not Auth")
  def MemberTypeNotBind = QianException(2207, "Member Type was not binding with Weixin")
  def MenuNotFound = QianException(2208, "Menu Not Found")
  def AuthNotFound = QianException(2209, "Auth Not Found")
  def RefreshTokenNotFound = QianException(2210, "RefreshToken Not Found")
  def TicketAlreadyInvalid = QianException(2211, "ticket already invalid")
  def AppIdAlreadyExists = QianException(2212, "appId already exists")
  def ShopSubMchIdExists = QianException(2213, "Shop SubMchId Exists")
  def SendUserNotNull = QianException(2214, "send to user not null")
  def MPError = QianException(2215, "MP Error")
  def MPNotFound = QianException(2216, "MP Not Found")
  def MPNotAction = QianException(2217, "MP Not Action")
  def MPMemberExist = QianException(2218, "MP Member Exist")
  def MemberTypeNotBindShop = QianException(2219, "MemberType Not Binding Shop")
  def MiniProgLoginError = QianException(2220, "MiniProgram Login Error")
  def MiniProgError = QianException(2221, "WeChat MiniProgram Error")
}

// agent 异常 23
sealed trait AgentErrors {
  def AgentNotConnected = QianException(2301, "Agent Not Connected")
  def NoMessageToAgent = QianException(2302, "No message sent to agent")
  def NoMessageBody = QianException(2303, "No message body")
  def WxPayError = QianException(2304, "Tenpay Error")
  def WxUserPaying = QianException(2305, "User is paying")
  def AckPayError = QianException(2306, "Ack Pay Error")
  def WxOrderPaid = QianException(2307, "Order Was Paid")
  def WxOrderClosed = QianException(2308, "Order Was Closed")
  def WxTradeRepeated = QianException(2309, "Trade Was Repeated")
  def ShopServiceNotEffected = QianException(2310, "Shop Service Not Effected")
}

// 返回给收银机的异常 24
sealed trait PosErrors {
  def InvalidBindToken = QianException(2401, "Invalid Bind Token")
  def PendingBindPos = QianException(2402, "Pending ACKing Token")
  def MasterNotFound = QianException(2403, "Master Not Found")
  def NotBindingManager = QianException(2404, "Not Binding Manager")
}

// 支付宝错误 25
trait AliErrors {
  def AliNoSign = QianException(2501, "No Sign In Ali Response")
  def AliSignVerifyFailed = QianException(2502, "Ali Sign Not Valid")
  def AliNoResponse = QianException(2503, "No Response In Json")
  def AliConfigError = QianException(2504, "Ali Config Error")
  def AliError = QianException(2505, "Ali Error")
}

// 威富通错误 26
trait SwiftErrors {
  def SwiftError = QianException(2601, "Swift Error")
  def SwiftNotFound = QianException(2602, "Swift Not Found")
}

trait ApiErrors
    extends SystemErrors
    with RequestErrors
    with TimeErrors
    with CouponErrors
    with VoucherErrors
    with ShopErrors
    with CashierErrors
    with MemberErrors
    with GoodsErrors
    with WeixinErrors
    with AgentErrors
    with PosErrors
    with AliErrors
    with SwiftErrors

object ApiErrors extends ApiErrors
