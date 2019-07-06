import sbt.Keys._
import sbt._

object Settings {
  
  private val scala212Options =
    Seq(
      "-encoding",
      "utf8",
      "-unchecked",
      "-feature",
      "-deprecation",
      "-target:jvm-1.8",
      "-language:_",
      "-Ypartial-unification",
      "-Xsource:2.13",
      "-Ycache-plugin-class-loader:last-modified",
      "-Ycache-macro-class-loader:last-modified",
      "-Xplugin-require:macroparadise",
    )

  private val javac8Options =
    Seq(
      "-source",
      "1.8",
      "-target",
      "1.8",
      "-Xlint:unchecked",
      "-Xlint:deprecation",
    )

  lazy val common = Seq(
    version := Version.mvno,
    scalaVersion := Version.scala,
    updateOptions := updateOptions.value.withCachedResolution(true),
    scalacOptions := scala212Options,
    javacOptions := javac8Options,
    addCompilerPlugin("com.olegpy"      %% "better-monadic-for" % "0.2.4"),
    addCompilerPlugin("org.spire-math"  %% "kind-projector" % Version.kindprojector),
    addCompilerPlugin("org.scalamacros" %% "paradise" % Version.macroParadise cross CrossVersion.full),
    libraryDependencies += "org.scala-lang" % "scala-reflect" % Version.scala,
  )

}
