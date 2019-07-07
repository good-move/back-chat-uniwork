package ru.nsu.fit.supernova.chat.web

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import cats.MonadError
import ru.nsu.fit.supernova.chat.config.ServerConfig

class Server[F[_]: MonadError[?[_], Throwable]](config: ServerConfig)(modules: HttpModule*)(
    implicit
    system: ActorSystem,
    mat: Materializer
) {

  def start(): Future[Http.ServerBinding] = {
    val moduleRoutes = modules.map(_.route).reduce(_ ~ _)
    val routes       = moduleRoutes
    Http().bindAndHandle(routes, config.interface, config.port)
  }

}
