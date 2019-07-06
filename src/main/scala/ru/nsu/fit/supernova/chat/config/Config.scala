package ru.nsu.fit.supernova.chat.config

final case class Config(server: ServerConfig, db: DbConfig)
final case class ServerConfig(interface: String, port: Int)
final case class DbConfig(mongo: MongoConfig)
final case class MongoConfig(uri: String, collections: MongoCollections)
final case class MongoCollections(chat: String)
