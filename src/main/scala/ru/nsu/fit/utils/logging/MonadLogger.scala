package ru.nsu.fit.utils.logging

trait MonadLogger[F[_]] {
  def debug(message: => String, mdc: => Map[String, Any] = Map.empty): F[Unit]
  def info(message: => String, mdc: => Map[String, Any] = Map.empty): F[Unit]
  def warning(message: => String, mdc: => Map[String, Any] = Map.empty): F[Unit]
  def error(message: => String, mdc: => Map[String, Any]): F[Unit]
  def error(cause: Throwable, message: => String, mdc: => Map[String, Any] = Map.empty): F[Unit]
}
