import sbt._

object Dependencies {

  val postgres = Seq(
    "io.getquill" %% "quill-async-postgres" % Version.quill,
    "io.getquill" %% "quill-jdbc" % Version.quill,
  )

  val mongo = Seq(
    "org.reactivemongo" %% "reactivemongo" % Version.mongo,
  )

  val monocle = Seq(
    "com.github.julien-truffaut" %% "monocle-core" % Version.monocle,
    "com.github.julien-truffaut" %% "monocle-macro" % Version.monocle,
  )

  val akka = Seq(
    "com.typesafe.akka" %% "akka-http" % Version.akkaHttp,
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-stream" % Version.akka,
    "com.typesafe.akka" %% "akka-remote" % Version.akka,
    "com.typesafe.akka" %% "akka-slf4j" % Version.akka,
  )

  val tcbTypedSchema = Seq(
    "ru.tinkoff" %% "typed-schema" % Version.`typed-schema`,
  )

  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % Version.logback,
    "ch.qos.logback" % "logback-core" % Version.logback,
    "net.logstash.logback" % "logstash-logback-encoder" % Version.logstashEncoder,
  )

  val json = Seq(
    "io.circe"          %% "circe-core" % Version.circe,
    "io.circe"          %% "circe-generic" % Version.circe,
    "io.circe"          %% "circe-parser" % Version.circe,
    "io.circe"          %% "circe-optics" % Version.circe,
    "io.circe"          %% "circe-config" % Version.circeConfig,
    "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpCirce,
  )

  val cats = Seq(
    "org.typelevel" %% "cats-core" % Version.cats,
    "org.typelevel" %% "kittens" % Version.kittens,
  )

  val enumeratum = Seq(
    "com.beachape" %% "enumeratum-circe",
    "com.beachape" %% "enumeratum-reactivemongo-bson",
  ).map(_ % Version.enumeratum)

  val html = Seq(
    "org.webjars.npm" % "swagger-ui-dist" % Version.swaggerUi,
    "com.lihaoyi" %% "scalatags" % Version.scalatags,
  )

  val derivation = Seq(
    "org.manatki" %% "derevo-tschema" % _,
    "org.manatki" %% "derevo-circe" % _,
    "org.manatki" %% "derevo-core" % _,
    "org.manatki" %% "derevo-rmongo" % _,
  ).map(_.apply(Version.derevo))

  val testing = Seq(
    "com.typesafe.akka" %% "akka-testkit" % Version.akka % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % Test,
    "org.scalatest"     %% "scalatest" % Version.scalatest % Test,
    "org.scalamock"     %% "scalamock" % Version.scalamock % Test,
    "org.scalacheck"    %% "scalacheck" % Version.scalacheck % Test,
  )

  val shapeless = Seq(
    "com.chuusai" %% "shapeless" % Version.shapeless,
  )

  lazy val `cats-mtl` = Seq(
    "org.typelevel" %% "cats-mtl-core" % "0.4.0",
    "org.typelevel" %% "cats-tagless-macros" % "0.5",
  )

  lazy val sttp = Seq(
    "com.softwaremill.sttp" %% "core",
    "com.softwaremill.sttp" %% "async-http-client-backend-monix",
    "com.softwaremill.sttp" %% "akka-http-backend",
    "com.softwaremill.sttp" %% "circe"
  ).map(_ % Version.sttp)

  lazy val config = Seq(
    "com.github.pureconfig" %% "pureconfig" % Version.pureconfig
  )

}

object Version {
  val scala = "2.12.8"
  val mvno  = "1.19.0"

  val `tcb-tools`     = "7.5.0"
  val tagging         = "2.2.1"
  val jwt             = "1.2.2"
  val akkaHttp        = "10.1.7"
  val akka            = "2.5.19"
  val akkaHttpCirce   = "1.23.0"
  val sttp            = "1.5.11"
  val monocle         = "1.5.1-cats"
  val quill           = "3.2.0"
  val h2              = "1.4.192"
  val postgres        = "42.2.5"
  val mongo           = "0.17.0"
  val `typed-schema`  = "0.10.4"
  val logback         = "1.2.3"
  val logstashEncoder = "5.2"
  val circe           = "0.11.0"
  val circeConfig     = "0.6.0"
  val cats            = "1.6.1"
  val kittens         = "1.2.0"
  val enumeratum      = "1.5.13"
  val swaggerUi       = "3.18.1"
  val scalatags       = "0.6.7"
  val scalatest       = "3.0.5"
  val scalamock       = "4.1.0"
  val scalacheck      = "1.14.0"
  val kindprojector   = "0.9.9"
  val macroParadise   = "2.1.0"
  val shapeless       = "2.3.3"
  val derevo          = "0.8.0"
  val pureconfig      = "0.11.1"
}
