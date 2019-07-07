package ru.nsu.fit.supernova.chat

import scala.concurrent.Future

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Directives.complete
import cats.~>
import io.circe.Printer

import ru.tinkoff.tschema.akkaHttp.Routable

package object web {

  implicit val jsonPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit def routableF[F[_], A: ToResponseMarshaller](implicit nt: F ~> Future): Routable[F[A], A] =
    a => complete(nt(a))

}
