package ru.nsu.fit.supernova.chat.web

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import ru.tinkoff.tschema.swagger.SwaggerBuilder

trait HttpModule extends FailFastCirceSupport {

  def route: Route
  def swagger: SwaggerBuilder

}
