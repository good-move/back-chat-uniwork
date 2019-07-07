package ru.nsu.fit.supernova.chat

import java.util.UUID

import monocle.macros.Lenses
import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive
import org.manatki.derevo.reactivemongoDerivation.{bsonDocumentReader, bsonDocumentWriter}
import reactivemongo.bson.Macros.Annotations.Key

package object model {

  type UserId = String
  type ChatId = String

  @derive(encoder, decoder, bsonDocumentReader, bsonDocumentWriter)
  final case class Message(id: String, authorId: UserId, text: String)

  @Lenses
  @derive(encoder, decoder, bsonDocumentReader, bsonDocumentWriter)
  final case class ChatRoom(
      @Key("_id") id: ChatId,
      status: ChatStatus,
      participants: Set[UserId],
      history: Seq[Message],
      nonce: Int = 1,
  )
  object ChatRoom {
    private def mkChatId: ChatId = UUID.randomUUID().toString

    def empty(): ChatRoom = ChatRoom(mkChatId, ChatStatus.Active, Set.empty, Seq.empty)
    def withUsers(participants: Set[UserId]): ChatRoom =
      ChatRoom(mkChatId, ChatStatus.Active, participants, Seq.empty)
  }

}
