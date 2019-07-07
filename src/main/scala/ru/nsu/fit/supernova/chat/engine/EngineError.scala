package ru.nsu.fit.supernova.chat.engine

import scala.util.control.NoStackTrace

import ru.nsu.fit.supernova.chat.model.ChatId

sealed abstract class EngineError(message: String) extends Throwable(message) with NoStackTrace
final case class ChatNotFound(chatId: ChatId) extends EngineError(s"Chat with id $chatId not found")
