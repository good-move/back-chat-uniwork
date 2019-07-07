package ru.nsu.fit.supernova.chat.web

import scala.concurrent.Future

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ws.UpgradeToWebSocket
import akka.http.scaladsl.server.Route
import cats.{Functor, ~>}
import cats.syntax.functor._
import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive
import org.manatki.derevo.tschemaInstances.swagger
import ru.nsu.fit.supernova.chat.engine.ChatEngine
import ru.nsu.fit.utils.tsatom.{HttpLog, PathEnd, websocket}

import ru.tinkoff.tschema.akkaHttp.MkRoute
import ru.tinkoff.tschema.swagger.{MkSwagger, SwaggerBuilder}

class ChatModule[F[_]: Functor: HttpLog](engine: ChatEngine[F])(implicit nt: F ~> Future) extends HttpModule {
  import ChatModule._

  val route: Route = MkRoute(schema)(this)

  def create(body: CreateChatRequest): F[String] = engine.create(body.participants).map(_.id)
  def connect(roomId: String, userId: String, ws: UpgradeToWebSocket): F[HttpResponse] =
    engine.connect(roomId, userId).map { handler =>
      ws.handleMessagesWithSinkSource(handler.sink, handler.source)
    }

  override def swagger: SwaggerBuilder = ???
}

object ChatModule {
  import ru.tinkoff.tschema.syntax._

  // format: off
  private val schema = tagPrefix('chat) :> prefix('rooms) :> (
    (capture[String]('roomId) :> queryParam[String]('userId) :> PathEnd :> get :> websocket('ws) :> key('connect) :> HttpLog :> $$[HttpResponse]) <|>
    (PathEnd :> post :> reqBody[CreateChatRequest] :> key('create) :> HttpLog :> $$[String])
  )
  // format: on

  @derive(decoder, swagger)
  final case class CreateChatRequest(participants: List[String])

}
