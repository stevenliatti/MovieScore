/**
 * Movie Score Parser
 * From JSON movies data, create Neo4j database with nodes
 * and relationships between Movies, Peoples and Genres
 * Jeremy Favre & Steven Liatti
 */

package ch.hepia

import pureconfig.loadConfig

case class Config(database: DatabaseConfig)
case class DatabaseConfig(url: String, username: String, password: String)

/**
 * Read app config
 */
object Config {
  def load(): Config = loadConfig[Config] match {
    case Right(config) => config
    case Left(error) =>
      throw new RuntimeException("Cannot read config file, errors:\n" + error.toList.mkString("\n"))
  }
}

