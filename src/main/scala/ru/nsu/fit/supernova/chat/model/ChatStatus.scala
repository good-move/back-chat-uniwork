package ru.nsu.fit.supernova.chat.model

import scala.collection.immutable

import enumeratum.EnumEntry.Snakecase
import enumeratum.{CirceEnum, Enum, ReactiveMongoBsonEnum}

sealed trait ChatStatus extends Snakecase
object ChatStatus extends Enum[ChatStatus] with ReactiveMongoBsonEnum[ChatStatus] with CirceEnum[ChatStatus] {
  override def values: immutable.IndexedSeq[ChatStatus] = findValues

  case object Active extends ChatStatus
  case object Deleted extends ChatStatus

}
