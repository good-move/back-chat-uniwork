package ru.nsu.fit.utils.tsatom

import akka.http.scaladsl.server.directives.PathDirectives.pathEnd
import shapeless.HList

import ru.tinkoff.tschema.akkaHttp.Serve
import ru.tinkoff.tschema.swagger.SwaggerMapper
import ru.tinkoff.tschema.typeDSL.DSLAtom

object PathEnd extends DSLAtom {

  implicit def serve[In <: HList]: Serve.Aux[PathEnd.type, In, In] = Serve.serveCheck(pathEnd)

  implicit def swagger: SwaggerMapper[PathEnd.type] = SwaggerMapper.empty[PathEnd.type]

}
