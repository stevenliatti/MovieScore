package ch.hepia

import ch.hepia.Domain._
import neotypes.Driver
import neotypes.implicits._
import spray.json.JsonParser

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

class MovieService(driver: Driver[Future]) {

  def addMovie(movie: Movie): Future[Unit] = driver.readSession { session =>
    val score: Double = movie.revenue.toDouble / movie.budget.toDouble

    c"""CREATE (movie: Movie {
        id: ${movie.id},
        title: ${movie.title},
        budget:${movie.budget},
        revenue:${movie.revenue},
        score: $score
     })""".query[Unit].execute(session)
  }

  def addGenres(genre: Genre, m: Movie): Future[Unit] = driver.readSession { session =>
      val f = c"""MERGE (genre: Genre {
        id: ${genre.id},
        name: ${genre.name}})""".query[Unit].execute(session)

      Await.result(f, Duration.Inf)

      c"""MATCH (m: Movie {id: ${m.id}})
          MATCH (g: Genre {id: ${genre.id}})
          MERGE (m)-[r:BELONGS_TO]->(g)
       """.query[Unit].execute(session)
  }

  def addPeople(people: People, m: Movie): Future[Unit] = driver.readSession { session =>
    people match {
      case a: Actor => {
        val f = c"""MERGE (p:People {
          id: ${people.id},
          name: ${people.name},
          gender: ${intToGender(people.gender)}
        })
        SET p: Actor""".query[Unit].execute(session)

        Await.result(f, Duration.Inf)

        c"""MATCH (m: Movie {id: ${m.id}})
        MATCH (p: Actor {id: ${people.id}})
        MERGE (p)-[r:PLAY_IN {character: ${a.character}}]->(m)
        """.query[Unit].execute(session)
      }
      case mm: MovieMaker => {
        val f = c"""MERGE (p:People {
          id: ${people.id},
          name: ${people.name},
          gender: ${intToGender(people.gender)}
        })
        SET p:MovieMaker""".query[Unit].execute(session)

        Await.result(f, Duration.Inf)

        c"""MATCH (m: Movie {id: ${m.id}})
        MATCH (p: MovieMaker {id: ${people.id}})
        MERGE (p)-[r:WORK_IN {job: ${mm.job}}]->(m)
        """.query[Unit].execute(session)
      }
    }
  }

  def addKnownForRelation(people: People, genre: Genre) : Future[Unit] = driver.readSession { session =>
    val knownFor = people match {
      case _: Actor => "KNOWS_FOR_ACTING"
      case _: MovieMaker => "KNOWN_FOR_WORKING"
    }
    c"""MATCH (p: People {id: ${people.id}})
        MATCH (g: Genre {id: ${genre.id}})
        MERGE (p)-[r:$knownFor]->(g)
          ON CREATE SET r.count = 1
          ON MATCH SET r.count = r.count+1
     """.query[Unit].execute(session)
  }

  def addKnowsRelation(people1: People, people2: People) : Future[Unit] = driver.readSession { session =>
    c"""MATCH (p1: People {id: ${people1.id}})
        MATCH (p2: People {id: ${people2.id}})
        MERGE (p1)-[r:KNOWS]-(p2)
          ON CREATE SET r.count = 1
          ON MATCH SET r.count = r.count+1
     """.query[Unit].execute(session)
  }

  def addSimilarRelation(movie: Movie, movieId: MovieId) : Future[Unit] = driver.readSession { session =>
    c"""MATCH (m1: Movie {id: ${movie.id}})
        MATCH (m2: Movie {id: ${movieId.id}})
        MERGE (m1)-[r:SIMILAR]-(m2)
     """.query[Unit].execute(session)
  }

  def addRecommendationsRelation(movie: Movie, movieId: MovieId) : Future[Unit] = driver.readSession { session =>
    c"""MATCH (m1: Movie {id: ${movie.id}})
        MATCH (m2: Movie {id: ${movieId.id}})
        MERGE (m1)-[r:RECOMMENDATIONS]-(m2)
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
