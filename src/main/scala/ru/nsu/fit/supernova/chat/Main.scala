package ru.nsu.fit.supernova.chat

import scala.concurrent.Future
import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.~>
import monix.eval.Task
import monix.execution.Scheduler
import pureconfig._
import reactivemongo.api.collections.bson.BSONCollection
import ru.nsu.fit.supernova.chat.config.Config
import ru.nsu.fit.supernova.chat.dao.{ChatRoomDao, ChatRoomDaoImpl}
import ru.nsu.fit.supernova.chat.engine.ChatEngineImpl
import ru.nsu.fit.supernova.chat.web.{ChatModule, Server}
import ru.nsu.fit.utils.logging.{PrettyLogger, ScalaLogger}
import ru.nsu.fit.utils.tsatom.HttpLog

object Main extends App {
  import pureconfig.generic.auto._

  implicit val system         = ActorSystem("supernova_actor_system")
  implicit val mat            = ActorMaterializer()
  val ctx                     = system.dispatcher
  implicit val monixScheduler = Scheduler(ctx)

  implicit val ntFT: Future ~> Task = new ~>[Future, Task] {
    override def apply[A](fa: Future[A]): Task[A] = Task.fromFuture(fa)
  }
  implicit val ntTF: Task ~> Future = new ~>[Task, Future] {
    override def apply[A](fa: Task[A]): Future[A] = fa.runToFuture
  }

  val logger      = new ScalaLogger("scala_logger")
  implicit val ML = new PrettyLogger[Task](logger)
  val config      = loadConfigOrThrow[Config]("ru.nsu.fit.supernova")

  implicit val httpLog: HttpLog[Task] = new HttpLog[Task]()

  val future = for {
    mongo <- dao.dbFuture(config.db.mongo.uri)
    dao: ChatRoomDao[Future] = new ChatRoomDaoImpl(mongo[BSONCollection](config.db.mongo.collections.chat))
    engine                   = new ChatEngineImpl[Task](dao)
    module                   = new ChatModule(engine)
    binding <- new Server[Task](config.server)(module).start()
  } yield binding

  future andThen {
    case Success(binding) =>
      logger.log.info("Successfully started server")(Map("host" -> binding.localAddress))
    case Failure(error) =>
      logger.log.error("Failed to start server", error)(Map.empty)
  }

}
