package ru.nsu.fit.utils.logging

import cats.Applicative
import cats.syntax.applicative._

class PrettyLogger[F[_]: Applicative](underlying: ScalaLogger) extends MonadLogger[F] {

  override def debug(message: => String, mdc: => Map[String, Any]): F[Unit] =
    underlying.log.debug(message)(mdc).pure[F]

  override def info(message: => String, mdc: => Map[String, Any]): F[Unit] =
    underlying.log.info(message)(mdc).pure[F]

  override def warning(message: => String, mdc: => Map[String, Any]): F[Unit] =
    underlying.log.warn(message)(mdc).pure[F]

  override def error(message: => String, mdc: => Map[String, Any]): F[Unit] =
    underlying.log.error(message)(mdc).pure[F]

  override def error(cause: Throwable, message: => String, mdc: => Map[String, Any]): F[Unit] =
    underlying.log.error(message, cause)(mdc).pure[F]

}

object PrettyLogger {

  def apply[F[_]: Applicative](name: String): PrettyLogger[F] = new PrettyLogger[F](new ScalaLogger(name))

}
