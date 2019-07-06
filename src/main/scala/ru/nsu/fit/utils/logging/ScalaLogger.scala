package ru.nsu.fit.utils.logging

import akka.http.scaladsl.model._
import com.typesafe.scalalogging.{CanLog, Logger, LoggerTakingImplicit}
import io.circe.{parser, Json, Printer}
import org.slf4j.MDC

final case class ScalaLogger(name: String) {
  import ScalaLogger._

  lazy val log: LoggerTakingImplicit[LogMDC] = Logger.takingImplicit[LogMDC](name)
}

object ScalaLogger {

  private val jsonPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  private implicit val canLog: CanLog[LogMDC] = new CanLog[LogMDC] {

    override def logMessage(originalMsg: String, mdc: LogMDC): String = {
      stringify(mdc).foreach { case (key, value) => MDC.put(key, value) }
      originalMsg
    }

    override def afterLog(a: LogMDC): Unit = MDC.clear()

  }

  private def stringify(map: LogMDC): Map[String, String] = {
    def flatten(map: Map[String, Any]): Map[String, Any] =
      map.flatMap {
        case (_, r: HttpMessage) => prettyHttpMessage(r)
        case (k, v: Throwable) =>
          Map(
            k -> Json.fromFields(
              Seq(
                "name"       -> Json.fromString(v.toString),
                "message"    -> Json.fromString(Option(v.getMessage).getOrElse("null message")),
                "stackTrace" -> Json.arr(getTrace(v).take(20).map(Json.fromString): _*)
              )
            )
          )
        case (k, v) => Map(k -> v)
      }

    flatten(map) mapValues {
      case v: Json   => v.pretty(jsonPrinter)
      case other     => other.toString
    }
  }

  private def prettyHttpMessage[T <: HttpMessage](message: T) = message match {
    case HttpRequest(method, uri, _, entity, _) =>
      Map(
        "phase"  -> "request",
        "method" -> method.value,
        "url"    -> uri.toString,
        "entity" -> prettyHttpEntity(entity)
      )
    case HttpResponse(status, _, entity, _) =>
      Map(
        "phase"  -> "response",
        "status" -> status.value,
        "entity" -> prettyHttpEntity(entity)
      )
  }

  private def prettyHttpEntity(entity: HttpEntity): Json = entity match {
    case HttpEntity.Strict(contentType, data) =>
      val stringData = data.utf8String
      contentType match {
        case ContentTypes.`application/json` =>
          parser.parse(stringData).getOrElse(Json.fromString(s"Failed to parse entity: ($stringData, $contentType)"))
        case ContentTypes.`text/plain(UTF-8)` => Json.fromString(stringData)
        case _                                => Json.fromString(s"Unsupported content type: $contentType")
      }

    case _: HttpEntity.Default => Json.fromString("Unknown entity type")
    case _                     => Json.fromString("Ignored entity type")
  }

  private def getTrace(error: Throwable): Seq[String] =
    error.getStackTrace.toSeq
      .map(el => s"${el.getClassName}.${el.getMethodName}(${el.getFileName}:${el.getLineNumber})")

  type LogMDC = Map[String, Any]

  def apply(implicit ev: ScalaLogger): ScalaLogger = ev

}
