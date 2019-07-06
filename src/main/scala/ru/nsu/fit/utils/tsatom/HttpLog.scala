package ru.nsu.fit.utils.tsatom

import java.util.UUID
import scala.concurrent.Future

import akka.http.scaladsl.model.{HttpEntity, HttpRequest, Uri}
import akka.http.scaladsl.server.Directives.{extractUri, logRequest, logResult}
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.server.util.Tuple
import akka.http.scaladsl.server.{Directive, RouteResult}
import cats.~>
import ru.nsu.fit.utils.logging.MonadLogger
import shapeless.HList

import ru.tinkoff.tschema.akkaHttp.Serve
import ru.tinkoff.tschema.swagger.SwaggerMapper
import ru.tinkoff.tschema.typeDSL.DSLAtom

class HttpLog[F[_]](implicit ML: MonadLogger[F], nt: F ~> Future) {

  def directive: Directive[Unit] = {
    val generatedCorrelationId = UUID.randomUUID().toString

    val requestLog = withUri { uri =>
      logRequest(LoggingMagnet { _ => req: HttpRequest =>
        {
          val requestMdc = Map(
            "correlationId" -> generatedCorrelationId,
            "phase"         -> "request",
            "name"          -> uri.path.toString(),
            "uri"           -> uri.toString(),
            "method"        -> req.method.value,
            "body" -> (req.entity match {
              case HttpEntity.Strict(_, data) => data.utf8String
              case _                          => "not strict entity"
            })
          )
          nt(ML.info("http request", requestMdc))
        }
      })
    }

    val responseLog = withUri { uri =>
      val commonMdc = Map(
        "correlationId" -> generatedCorrelationId,
        "phase"         -> "response",
        "name"          -> uri.path.toString(),
      )
      logResult(LoggingMagnet { _ =>
        {
          case RouteResult.Rejected(rejections) =>
            nt(
              ML.info(
                s"Request was rejected with rejections: \n$rejections",
                commonMdc
              )
            )
          case RouteResult.Complete(response) =>
            val responseMdc = Map(
              "status" -> response.status.value,
              "body" -> (response.entity match {
                case HttpEntity.Strict(_, data) => data.utf8String
                case _                          => "not strict entity"
              })
            )
            nt(ML.info("http response", responseMdc ++ commonMdc))
        }
      })
    }

    requestLog.tflatMap(_ => responseLog)
  }

  private def withUri[R](dir: Uri => Directive[R])(implicit ev: Tuple[R]) = extractUri.tflatMap { uri =>
    dir(uri._1)
  }

}

object HttpLog extends DSLAtom {
  implicit def serve[F[_], In <: HList](implicit logging: HttpLog[F]): Serve.Check[HttpLog.type, In] =
    Serve.serveCheck(logging.directive)

  implicit def swagger[F[_]]: SwaggerMapper[HttpLog.type] = SwaggerMapper.empty
}
