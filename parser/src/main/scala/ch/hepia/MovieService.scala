/**
 * Movie Score Parser
 * From JSON movies data, create Neo4j database with nodes
 * and relationships between Movies, Peoples and Genres
 * Jeremy Favre & Steven Liatti
 */

package ch.hepia

import ch.hepia.Domain._
import neotypes.Driver
import neotypes.implicits._
import spray.json.JsonParser

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

/**
 * Main service class who read JSON movies
 * and give methods to add nodes and relationships
 * in Neo4j with Cypher requests
 * @param driver neo4j scala driver
 */
class MovieService(driver: Driver[Future]) {

  private val movieMakerScoreDivisor = 50.0

  private def computeMovieScore(movie: Movie): Double = {
    movie.revenue.toDouble / movie.budget.toDouble
  }

  def addMovie(movie: Movie): Future[Unit] = driver.readSession { session =>
    val score: Double = computeMovieScore(movie)

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

  private def sumPeopleScore(people: People, movie: Movie): Double = {
    val movieScore = computeMovieScore(movie)
    val peopleRank = people match {
      case a: Actor => (Main.actorsByMovie - a.order).toDouble
      case _: MovieMaker => movieMakerScoreDivisor
    }
    movieScore / peopleRank
  }

  def addPeople(people: People, m: Movie): Future[Unit] = driver.readSession { session =>
    val peopleScore = sumPeopleScore(people, m)
    people match {
      case a: Actor =>
          val f = c"""MERGE (p:People {
          id: ${people.id},
          name: ${people.name},
          gender: ${people.intToGender()}
        })
        ON CREATE SET p.score = $peopleScore
        ON MATCH SET p.score = p.score+$peopleScore
        SET p: Actor""".query[Unit].execute(session)

        Await.result(f, Duration.Inf)

        c"""MATCH (m: Movie {id: ${m.id}})
        MATCH (p: Actor {id: ${people.id}})
        MERGE (p)-[r:PLAY_IN {character: ${a.character}}]->(m)
        """.query[Unit].execute(session)
      case mm: MovieMaker =>
        val f = c"""MERGE (p:People {
          id: ${people.id},
          name: ${people.name},
          gender: ${people.intToGender()}
        })
        ON CREATE SET p.score = $peopleScore
        ON MATCH SET p.score = p.score+$peopleScore
        SET p: MovieMaker""".query[Unit].execute(session)

        Await.result(f, Duration.Inf)

        c"""MATCH (m: Movie {id: ${m.id}})
        MATCH (p: MovieMaker {id: ${people.id}})
        MERGE (p)-[r:WORK_IN {job: ${mm.job}}]->(m)
        """.query[Unit].execute(session)
    }
  }

  def computeFinalPeopleScore(people: People): Future[Unit] = driver.readSession { session =>
    val f = c"""MATCH (People {id: ${people.id}})-->(m:Movie) RETURN count(*)"""
      .query[Int].single(session)
    
    val count = Await.result(f, Duration.Inf)

    c"""MATCH (p:People {id: ${people.id}})
        SET p.score = p.score / $count
        RETURN p""".query[Unit].execute(session)
  }

  def addKnownForRelation(people: People, genre: Genre) : Future[Unit] = driver.readSession { session =>
    people match {
      case _: Actor =>
        c"""MATCH (p: People {id: ${people.id}})
        MATCH (g: Genre {id: ${genre.id}})
        MERGE (p)-[r:KNOWN_FOR_ACTING]->(g)
          ON CREATE SET r.count = 1
          ON MATCH SET r.count = r.count+1
     """.query[Unit].execute(session)
      case _: MovieMaker =>
        c"""MATCH (p: People {id: ${people.id}})
        MATCH (g: Genre {id: ${genre.id}})
        MERGE (p)-[r:KNOWN_FOR_WORKING]->(g)
          ON CREATE SET r.count = 1
          ON MATCH SET r.count = r.count+1
     """.query[Unit].execute(session)
    }
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

  def readMoviesFromFile(path: String): Iterator[Movie] = {
    import JsonFormats._

    Source.fromFile(path).getLines
      .map(line => JsonParser(line).convertTo[Movie])
  }

}
