package ch.hepia

import pureconfig.loadConfig

case class Config(database: DatabaseConfig)
case class DatabaseConfig(url: String, username: String, password: String)

object Config {
  def load() =
    loadConfig[Config] match {
      case Right(config) => config
      case Left(error) =>
        throw new RuntimeException("Cannot read config file, errors:\n" + error.toList.mkString("\n"))
    }
}

