package ru.nsu.fit.supernova.chat

import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive
import org.manatki.derevo.reactivemongoDerivation.{bsonDocumentReader, bsonDocumentWriter}
import reactivemongo.bson.Macros.Annotations.Key

package object model {

  type UserId = String

  @derive(encoder, decoder, bsonDocumentReader, bsonDocumentWriter)
  final case class Message(id: String, authorId: UserId, text: String)

  @derive(encoder, decoder, bsonDocumentReader, bsonDocumentWriter)
  final case class ChatRoom(
      @Key("_id") id: String,
      status: ChatStatus,
      participants: Set[UserId],
      history: Seq[Message],
      nonce: Int = 1,
  )

}
