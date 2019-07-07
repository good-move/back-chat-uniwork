package ru.nsu.fit.supernova.chat.dao

import scala.language.higherKinds

import ru.nsu.fit.supernova.chat.model.{ChatRoom, UserId}

trait ChatRoomDao[F[_]] {

  def find(chatId: String): F[Option[ChatRoom]]
  def insert(chatRoom: ChatRoom): F[ChatRoom]
  def update(chatRoom: ChatRoom): F[ChatRoom]
  def active(): F[Seq[ChatRoom]]
  def ofUser(userId: UserId): F[Seq[ChatRoom]]

}
