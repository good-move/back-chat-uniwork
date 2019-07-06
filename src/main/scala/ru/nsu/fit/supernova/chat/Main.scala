package ru.nsu.fit.supernova.chat

import scala.concurrent.Future
import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import pureconfig._
import reactivemongo.api.collections.bson.BSONCollection
import ru.nsu.fit.supernova.chat.config.Config
import ru.nsu.fit.supernova.chat.dao.{ChatRoomDao, ChatRoomDaoImpl}
import ru.nsu.fit.supernova.chat.model.{ChatRoom, ChatStatus}

object Main extends App {
  import pureconfig.generic.auto._

  implicit val system = ActorSystem("supernova_actor_system")
  implicit val ctx    = system.dispatcher

  val config = loadConfigOrThrow[Config]("ru.nsu.fit.supernova")

  val chatRoom = ChatRoom("123", ChatStatus.Active, Set.empty, List.empty)

  val future = for {
    mongo <- dao.dbFuture(config.db.mongo.uri)
    dao: ChatRoomDao[Future] = new ChatRoomDaoImpl(mongo[BSONCollection](config.db.mongo.collections.chat))
    _  <- dao.insert(chatRoom)
    cr <- dao.find(chatRoom.id)
    _ = println(cr)
  } yield ()


  future.andThen {
    case Success(value) => println(value)
    case Failure(value) => println(value)
  }

}
