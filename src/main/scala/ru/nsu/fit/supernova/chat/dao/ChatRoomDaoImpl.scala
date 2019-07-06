package ru.nsu.fit.supernova.chat.dao

import scala.concurrent.{ExecutionContext, Future}

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument}
import com.github.dwickern.macros.NameOf.nameOf
import reactivemongo.api.Cursor
import ru.nsu.fit.supernova.chat.model.{ChatRoom, ChatStatus, UserId}

class ChatRoomDaoImpl(collection: BSONCollection)(implicit ec: ExecutionContext) extends ChatRoomDao[Future] {
  import ChatRoomDaoImpl._

  override def find(chatId: String): Future[Option[ChatRoom]] =
    collection.find(BSONDocument("_id" -> chatId), None).one[ChatRoom]

  override def insert(chatRoom: ChatRoom): Future[ChatRoom] =
    collection.insert(ordered = false).one(chatRoom).flatMap { wr =>
      if (wr.n == 1 && wr.ok) Future.successful(chatRoom)
      else Future.failed(InsertionError(wr.writeErrors.map(_.errmsg).mkString("InsertionErrors[", ", ", "]")))
    }

  override def update(chatRoom: ChatRoom): Future[ChatRoom] = {
    val patched = chatRoom.copy(nonce = chatRoom.nonce + 1)
    collection
      .findAndUpdate(BSONDocument(Keys.id -> chatRoom.id, Keys.nonce -> chatRoom.nonce), patched)
      .map(_.result[ChatRoom].get)
  }

  override def active(): Future[Seq[ChatRoom]] =
    collection
      .find(BSONDocument(Keys.status -> ChatStatus.Active.toString), Option.empty)
      .cursor[ChatRoom]()
      .collect[Seq](maxDocs = -1, Cursor.ContOnError())

  override def ofUser(userId: UserId): Future[Seq[ChatRoom]] =
    collection
      .find(
        BSONDocument(
          Keys.status -> ChatStatus.Active.toString,
          BSONDocument("participants" -> BSONDocument("$all" -> BSONArray(userId))),
        ),
        Option.empty
      )
      .cursor[ChatRoom]()
      .collect[Seq](maxDocs = -1, Cursor.ContOnError())

}

object ChatRoomDaoImpl {

  object Keys {
    val id                   = "_id"
    val nonce: String        = nameOf[ChatRoom](_.nonce)
    val status: String       = nameOf[ChatRoom](_.status)
    val participants: String = nameOf[ChatRoom](_.participants)
  }

}
