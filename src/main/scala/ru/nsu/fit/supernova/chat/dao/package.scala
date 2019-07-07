package ru.nsu.fit.supernova.chat

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

import reactivemongo.api.{DefaultDB, MongoConnection}

package object dao {

  def dbFuture(uri: String)(implicit ec: ExecutionContext): Future[DefaultDB] = {
    val driver = new reactivemongo.api.MongoDriver
    for {
      uri <- Future.fromTry(MongoConnection.parseURI(uri))
      con = driver.connection(uri, strictUri = true).get
      dn <- Future(uri.db.get)
      db <- con.database(dn)
    } yield db
  }

  abstract class DaoError(message: String) extends Throwable with NoStackTrace
  final case class InsertionError(message: String) extends DaoError(message)

}
