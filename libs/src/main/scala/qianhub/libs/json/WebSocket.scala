package qianhub.libs.json

import io.circe.Json
import qianhub.libs.model.SN

// WebSocket 双向格式都一致, action 是操作, message 是正确消息内容, error 是异常信息,
// tag 是消息标识, 如果是回复对方消息, 也可以使用这个, 如果有值, 就说明需要对方返回并获取值
trait WsTo {
  def action: String
  def message: Option[Json]
  def error: Option[Json]
  def tag: Option[SN]
}

trait WsFormat[T <: WsTo] {
  def from(action: String, value: Option[Json], error: Option[Json], tag: Option[SN]): T
}

final case class WsToServer(
    action: String,
    message: Option[Json] = None,
    error: Option[Json] = None,
    tag: Option[SN] = None)
    extends WsTo

final case class WsToClient(
    action: String,
    message: Option[Json] = None,
    error: Option[Json] = None,
    tag: Option[SN] = None)
    extends WsTo

final case class WsToOss(
    action: String,
    message: Option[Json] = None,
    error: Option[Json] = None,
    tag: Option[SN] = None)
    extends WsTo

object WsToServer {
  implicit val Format = new WsFormat[WsToServer] {
    def from(action: String, value: Option[Json], error: Option[Json], tag: Option[SN]): WsToServer = {
      WsToServer(action, value, error, tag)
    }
  }
}

object WsToClient {
  implicit val Format = new WsFormat[WsToClient] {
    def from(action: String, value: Option[Json], error: Option[Json], tag: Option[SN]): WsToClient = {
      WsToClient(action, value, error, tag)
    }
  }
}

object WsToOss {
  implicit val Format = new WsFormat[WsToOss] {
    def from(action: String, value: Option[Json], error: Option[Json], tag: Option[SN]): WsToOss = {
      WsToOss(action, value, error, tag)
    }
  }
}
