package ru.nsu.fit.utils.tsatom

import akka.http.scaladsl.model.headers.{Connection, Upgrade}
import akka.http.scaladsl.model.ws.UpgradeToWebSocket
import akka.http.scaladsl.server.directives.BasicDirectives
import akka.http.scaladsl.server.{UnsupportedWebSocketSubprotocolRejection, ValidationRejection}
import shapeless.labelled.FieldType
import shapeless.{::, HList, Witness}

import ru.tinkoff.tschema.akkaHttp.Serve
import ru.tinkoff.tschema.swagger.SwaggerMapper
import ru.tinkoff.tschema.typeDSL.DSLAtom

final case class ExtractWebSocket[name](w: Witness.Lt[name]) extends DSLAtom

object ExtractWebSocket {

  implicit def serve[name <: Symbol, In <: HList]
    : Serve.Aux[ExtractWebSocket[name], In, FieldType[name, UpgradeToWebSocket] :: In] =
    Serve.serveAdd[ExtractWebSocket[name], In, UpgradeToWebSocket, name](
      BasicDirectives.extractRequest
        .filter(
          _.header[Connection].map(_.hasUpgrade).fold(false)(identity),
          ValidationRejection("Connection header must contain 'upgrade'")
        )
        .filter(
          _.header[Upgrade].map(_.hasWebSocket).fold(false)(identity),
          ValidationRejection("Upgrade header must equal 'websocket'")
        )
        .map(_.header[UpgradeToWebSocket])
        .collect({
          case Some(value) => value
        }, UnsupportedWebSocketSubprotocolRejection("websocket"))
    )

  implicit def swagger[name]: SwaggerMapper[ExtractWebSocket[name]] = SwaggerMapper.empty[ExtractWebSocket[name]]

}

object websocket {

  val default = ExtractWebSocket[Symbol](Witness('ws))

  def apply[name <: Symbol](name: Witness.Lt[name]): ExtractWebSocket[name] = ExtractWebSocket(name)

  def apply(): ExtractWebSocket[Symbol] = default

}
