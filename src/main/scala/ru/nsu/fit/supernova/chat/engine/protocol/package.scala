package ru.nsu.fit.supernova.chat.engine

import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive
import ru.nsu.fit.supernova.chat.model.{ChatId, UserId}

package object protocol {

  sealed trait Message

  sealed trait ChatMessage extends Message
  final case class OpenRoom(participants: Set[UserId]) extends ChatMessage
  final case class CloseRoom(roomId: ChatId) extends ChatMessage

  @derive(decoder, encoder)
  final case class TextMessage(authorId: String, text: String)
}
