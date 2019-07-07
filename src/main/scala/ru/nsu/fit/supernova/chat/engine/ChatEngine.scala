package ru.nsu.fit.supernova.chat.engine

import ru.nsu.fit.supernova.chat.model.{ChatId, ChatRoom, UserId}

trait ChatEngine[F[_]] {

  def create(participants: Seq[UserId]): F[ChatRoom]
  def delete(chatId: ChatId): F[Unit]
  def connect(chatId: ChatId, userId: UserId): F[StreamConnectionHandler]

}
