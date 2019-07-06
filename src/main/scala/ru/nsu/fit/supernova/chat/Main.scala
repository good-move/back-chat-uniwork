package ru.nsu.fit.supernova.chat

import scala.util.Try

import akka.actor.ActorSystem
import io.circe.Json
import cats.instances.try_._
import ru.nsu.fit.utils.logging.{MonadLogger, PrettyLogger}

object Main extends App {

  implicit val system = ActorSystem("supernova_actor_system")

  val logger: MonadLogger[Try] = PrettyLogger[Try](name = "supernova_logger")

  logger.info("application started")
  logger.info("testing", Map("entity" -> Json.obj("f" -> Json.fromString("v"))))
}
