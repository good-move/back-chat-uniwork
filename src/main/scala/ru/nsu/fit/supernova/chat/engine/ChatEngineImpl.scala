package ru.nsu.fit.supernova.chat.engine

import java.util.UUID
import scala.concurrent.Future

import akka.actor.{ActorSystem, Props}
import cats.effect.Concurrent
import cats.effect.concurrent.Ref
import cats.{~>, MonadError}
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.flatMap._
import monix.execution.Scheduler
import ru.nsu.fit.supernova.chat.dao.ChatRoomDao
import ru.nsu.fit.supernova.chat.engine.EngineChatRoomActor.RoomWrapper
import ru.nsu.fit.supernova.chat.model.{ChatId, ChatRoom, UserId}
import ru.nsu.fit.utils.logging.MonadLogger

class ChatEngineImpl[F[_]: Concurrent](dao: ChatRoomDao[Future])(
    implicit
    ME: MonadError[F, Throwable],
    ML: MonadLogger[F],
    nt: Future ~> F,
    nt1: F ~> Future,
    s: Scheduler,
    system: ActorSystem
) extends ChatEngine[F] {

  private val ref = Ref.unsafe[F, Map[ChatId, EngineChatRoom[F]]](Map.empty)
  nt1(recoverChats())

  def find(id: ChatId): F[Option[EngineChatRoom[F]]] =
    ref.get.map(_.get(id))

  override def create(participants: Seq[UserId]): F[ChatRoom] = {
    val roomId = UUID.randomUUID().toString
    launchChatRoom(roomId, participants.toSet)
  }

  override def delete(chatId: ChatId): F[Unit] =
    ref
      .modify { map =>
        val maybeBcast = map.get(chatId)
        val updated    = map - chatId
        (updated, maybeBcast)
      }
      .map(_.fold(ME.unit)(_.close))
      .void

  override def connect(chatId: ChatId, userId: UserId): F[StreamConnectionHandler] =
    for {
      maybeRoom <- ref.get
      handler <- maybeRoom
        .get(chatId)
        .fold(ME.raiseError[StreamConnectionHandler](ChatNotFound(chatId)))(_.connect(userId))
    } yield handler

  private def launchChatRoom(id: String, participants: Set[UserId]): F[ChatRoom] = {
    val room = RoomWrapper(system.actorOf(Props(new EngineChatRoomActor[F](id, participants, dao))))
    for {
      _ <- ref.modify { map =>
        (map + (id -> room), ())
      }
      chatRoom <- room.chatRoom
      _        <- ML.info(s"Initialized chat room ${chatRoom.id}")
    } yield chatRoom
  }

  private def recoverChats(): F[Unit] =
    for {
      _           <- ML.info("recovering chat rooms")
      activeChats <- nt(dao.active())
      _ <- activeChats.foldLeft(ME.unit)(
        (acc, room) =>
          for {
            _ <- acc
            _ <- ML.info("recovering chat", Map("chatId" -> room.id))
            _ <- launchChatRoom(room.id, room.participants)
          } yield ()
      )

    } yield ()

}

object ChatEngineImpl {

  final case class EngineChatRoom(
      chatRoom: ChatRoom,
  )

}
