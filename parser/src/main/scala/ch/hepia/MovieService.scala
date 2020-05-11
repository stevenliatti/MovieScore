package ch.hepia

import ch.hepia.Domain._
import neotypes.Driver
import neotypes.implicits._
import spray.json.JsonParser

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

class MovieService(driver: Driver[Future]) {

  def insertMovie(movie: Movie): Future[Unit] = driver.readSession { session =>
    val score: Double = movie.revenue.toDouble / movie.budget.toDouble

    c"""CREATE (movie: Movie {
        id: ${movie.id},
        title: ${movie.title},
        budget:${movie.budget},
        revenue:${movie.revenue},
        score: $score
     })""".query[Unit].execute(session)
  }

  def insertGenres(genre: Genre, m: Movie): Future[Unit] = driver.readSession { session =>
      val f = c"""MERGE (genre: Genre {
        id: ${genre.id},
        name: ${genre.name}})""".query[Unit].execute(session)

      Await.result(f, Duration.Inf)

      c"""MATCH (m: Movie {id: ${m.id}})
          MATCH (g: Genre {id: ${genre.id}})
          MERGE (m)-[r:BELONGS_TO]->(g)
       """.query[Unit].execute(session)
  }

  def insertActor(actor: Actor, m: Movie): Future[Unit] = driver.readSession { session =>
    val f = c"""MERGE (actor:People:Actor {
        id: ${actor.id},
        name: ${actor.name},
        gender: ${intToGender(actor.gender)},
        character: ${actor.character}
      })""".query[Unit].execute(session)

    Await.result(f, Duration.Inf)

    c"""MATCH (m: Movie {id: ${m.id}})
        MATCH (a: Actor {id: ${actor.id}})
        MERGE (a)-[r:PLAY_IN {character: ${actor.character}}]->(m)
     """.query[Unit].execute(session)
  }

  def insertMovieMaker(movieMaker: MovieMaker, m: Movie): Future[Unit] = driver.readSession { session =>
    val f = c"""MERGE (mm:People:MovieMaker {
        id: ${movieMaker.id},
        name: ${movieMaker.name},
        gender: ${intToGender(movieMaker.gender)},
        job: ${movieMaker.job}
      })""".query[Unit].execute(session)

    Await.result(f, Duration.Inf)

    c"""MATCH (m: Movie {id: ${m.id}})
        MATCH (mm: MovieMaker {id: ${movieMaker.id}})
        MERGE (mm)-[r:WORK_IN {job: ${movieMaker.job}}]->(m)
     """.query[Unit].execute(session)
  }

  // TODO : FAIRE ERITAGE DE PEPOL OU PARIL POUR ACTOR
  def addKnownForRealtion(movieMaker: MovieMaker, genre: Genre) : Future[Unit] = driver.readSession { session =>
    c"""MATCH (mm: MovieMaker {id: ${movieMaker.id}})
        MATCH (g: Genre {id: ${genre.id}})
        MERGE (mm)-[r:KNOWS_FOR {genre: ${genre.name}}]->(g)
          ON CREATE SET r.count = 1
          ON MATCH SET r.count = r.count+1
     """.query[Unit].execute(session)
  }

  def readMoviesFromFile(path: String) = {
    import JsonFormats._

    Source.fromFile(path).getLines
      .map(line => JsonParser(line).convertTo[Movie])
  }

  def search(query: String): Future[Seq[Movie]] = driver.readSession { session =>
    c"""MATCH (movie:Movie)
        WHERE lower(movie.title) CONTAINS ${query.toLowerCase}
        RETURN movie""".query[Movie].list(session)
  }


}
