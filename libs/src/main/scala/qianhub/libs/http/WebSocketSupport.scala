package qianhub.libs.http

import akka.actor._
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server._
import akka.stream._
import akka.stream.scaladsl._
import io.circe._
import qianhub.libs.json.Jsons

import scala.concurrent.{ExecutionContext, Future}

// 对 WebSocket 提供支持(参考 playframework 的实现)
trait WebSocketSupport { self: ServerSupport =>

  // Json WebSocket
  final def jsonWS[In, Out](f: ActorRef => Props)(
      implicit e: Encoder[Out],
      d: Decoder[In],
      system: ActorSystem,
      ec: ExecutionContext): Route = {
    // Message => In
    def toIn: Flow[Message, In, Any] = {
      Flow[Message]
        .collect {
          case TextMessage.Strict(txt) => Future.fromTry(Jsons.from[In](txt))
          case TextMessage.Streamed(stream) =>
            stream.runFold("")(_ + _).flatMap { txt =>
              Future.fromTry(Jsons.from[In](txt))
            }
        }
        .mapAsync(1)(identity)
    }
    // Out => Message
    def fromOut: Flow[Out, Message, Any] = {
      Flow[Out].map { r =>
        val json = Jsons.toJson(r)
        TextMessage(Jsons.stringify(json))
      }
    }
    // In => Out
    val flow = actorRef[In, Out](f)
    val handler = toIn.via(flow).via(fromOut)
    handleWebSocketMessages(handler)
  }

  // 建立连接器
  final def actorRef[In, Out](
      props: ActorRef => Props,
      bufferSize: Int = 16,
      overflowStrategy: OverflowStrategy = OverflowStrategy.dropNew)(
      implicit system: ActorSystem): Flow[In, Out, _] = {

    val (outActor, publisher) =
      Source.actorRef[Out](bufferSize, overflowStrategy).toMat(Sink.asPublisher(false))(Keep.both).run()

    Flow.fromSinkAndSource(Sink.actorRef(system.actorOf(Props(new Actor {
      val flowActor = context.watch(context.actorOf(props(outActor), "flowActor"))

      def receive: Receive = {
        case Status.Success(_) | Status.Failure(_) => flowActor ! PoisonPill
        case Terminated(_)                         => context.stop(self)
        case other                                 => flowActor ! other
      }

      override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
        case _ => SupervisorStrategy.Stop
      }
    })), Status.Success(())), Source.fromPublisher(publisher))
  }
}
