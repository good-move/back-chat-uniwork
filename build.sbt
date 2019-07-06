lazy val _mainClass = Some("ru.nsu.fit.supernova.chat.Main")

lazy val digitalhack =
  (project in file("."))
    .settings(
      version := "0.1",
      name := "hackchat",
      scalaVersion := Version.scala,
      mainClass in (Compile, run) := _mainClass,
      mainClass in (Compile, reStart) := _mainClass,
    )
    .settings(Settings.common)
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.akka,
        Dependencies.postgres,
        Dependencies.mongo,
        Dependencies.sttp,
        Dependencies.cats,
        Dependencies.monocle,
        Dependencies.tcbTypedSchema,
        Dependencies.logging,
        Dependencies.json,
        Dependencies.enumeratum,
        Dependencies.derivation,
        Dependencies.testing,
      ).flatten
    )
    .settings(
      javaOptions in reStart ++= Seq(
        "-DCUSTOM_LOG_DIR=./target",
        "-Dconfig.file=src/main/resources/local.conf"
      ),
      javaOptions in run ++= Seq(
        "-DCUSTOM_LOG_DIR=./target",
        "-Dconfig.file=src/main/resources/local.conf"
      ),
    )
