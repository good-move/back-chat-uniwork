package ru.nsu.fit.supernova.chat.engine

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.higherKinds

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message => WsMessage, TextMessage => WsTextMessage}
import akka.pattern.ask
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy}
import akka.util.Timeout
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.{~>, Monad}
import io.circe.syntax._
import io.circe.{parser, Printer}
import monix.execution.Scheduler
import ru.nsu.fit.supernova.chat.dao.ChatRoomDao
import ru.nsu.fit.supernova.chat.engine.protocol.TextMessage
import ru.nsu.fit.supernova.chat.model.{ChatRoom, ChatStatus, UserId, Message => HistoryMessage}

trait EngineChatRoom[F[_]] {

  val id: F[String]
  def chatRoom: F[ChatRoom]
  def connect(userId: UserId): F[StreamConnectionHandler]
  def close: F[Unit]

}

final class EngineChatRoomActor[F[_]: Monad](_id: String, initialUsers: Set[UserId], dao: ChatRoomDao[Future])(
    implicit
    nt: Future ~> F,
    scheduler: Scheduler,
) extends Actor { roomActor =>
  import EngineChatRoomActor._

  implicit val mat     = ActorMaterializer()
  implicit val timeout = Timeout(5 seconds)

  private var room                         = ChatRoom.withUsers(initialUsers).copy(id = _id)
  private var users: Map[UserId, ActorRef] = Map.empty

  dao.insert(room)

  private val broadcast = (msg: TextMessage, filter: UserId => Boolean) => {
    users
      .filter { case (userId, _) => filter(userId) }
      .foreach {
        case (userId, ref) =>
          ref ! PostMessage(msg)

      }
  }

  override def receive: Receive = {
    case Connect(userId) =>
      val client = context.system.actorOf(
        Props(new Sender(msg => {
          val roomPatch =
            ChatRoom.history.modify(_ :+ HistoryMessage(UUID.randomUUID().toString, msg.authorId, msg.text))(room)
          dao.update(roomPatch).map { updated =>
            room = updated
          }
          broadcast(msg, _ != userId)
        })),
        s"${_id}_${userId}_${UUID.randomUUID().toString}"
      )
      val out = Source
        .actorRef(50, OverflowStrategy.dropNew)
        .mapMaterializedValue { actor =>
          client ! SetActor(actor)
          actor
        }

      val in = Sink.actorRef[WsMessage](client, RoomClosed)

      users += (userId -> client)

      sender() ! MessageSinkSource(in, out)

    case Disconnect(userId) =>
      users -= userId
      room = ChatRoom.participants.modify(_ - userId)(room)
      dao.update(room)

    case RoomClosed =>
      dao.update(room.copy(status = ChatStatus.Deleted))
      users
        .foreach {
          case (userId, ref) =>
            users -= userId
            ref ! RoomClosed
        }
        .pure[F]
      self ! PoisonPill

    case GetId       => sender() ! _id
    case GetChatRoom => sender() ! room.copy()
  }

}

object EngineChatRoomActor {
  case object GetId
  case object GetChatRoom

  sealed trait RoomMessage
  final case class Connect(userId: UserId) extends RoomMessage
  final case class Disconnect(userId: UserId) extends RoomMessage
  final case class PostMessage(message: TextMessage) extends RoomMessage
  case object RoomClosed extends RoomMessage

  case class MessageSinkSource(sink: Sink[WsMessage, _], source: Source[WsMessage, _]) extends StreamConnectionHandler

  final case class SetActor(actorRef: ActorRef)

  class Sender(handler: TextMessage => Unit)(implicit ec: ExecutionContext, mat: Materializer) extends Actor {
    private var wsActor: Option[ActorRef] = None

    override def receive: Receive = {
      case tm: WsTextMessage =>
        tm.toStrict(10 seconds).map { strictMsg =>
          val msg = parser.parse(strictMsg.text).right.flatMap(_.as[TextMessage]).right.get
          handler(msg)

        }

      case SetActor(ref) =>
        wsActor = Some(ref)
        context.watch(ref)

      case PostMessage(message) =>
        wsActor.foreach(_ ! WsTextMessage.Strict(message.asJson.pretty(jsonPrinter)))

      case RoomClosed => wsActor.foreach(_ ! PoisonPill)
      case e =>
        println("OTHER MESSAGE", e)
    }

  }

  implicit val jsonPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  case class RoomWrapper[F[_]: Monad](ref: ActorRef)(implicit nt: Future ~> F) extends EngineChatRoom[F] {
    implicit val timeout = Timeout(5 seconds)

    override val id: F[String] = nt(ref ? GetId).map(_.asInstanceOf[String])

    override def chatRoom: F[ChatRoom] = nt(ref ? GetChatRoom).map(_.asInstanceOf[ChatRoom])

    override def connect(userId: UserId): F[StreamConnectionHandler] =
      nt(ref ? Connect(userId)).map(_.asInstanceOf[StreamConnectionHandler])

    override def close: F[Unit] = (ref ! RoomClosed).pure[F]
  }

}
