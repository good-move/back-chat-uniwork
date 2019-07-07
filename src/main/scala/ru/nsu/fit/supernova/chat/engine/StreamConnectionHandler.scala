package ru.nsu.fit.supernova.chat.engine

import akka.http.scaladsl.model.ws.{Message => WsMessage}
import akka.stream.scaladsl.{Sink, Source}

trait StreamConnectionHandler {

  val source: Source[WsMessage, _]
  val sink: Sink[WsMessage, _]

}
