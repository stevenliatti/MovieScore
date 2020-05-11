package ch.hepia

import ch.hepia.Domain.{Credits, Genre, Movie}
import neotypes.Driver
import neotypes.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

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

    // insertCredits(movie.credits)
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

  private def insertCredits(credits: Credits): Future[Unit] = driver.readSession { session =>
    ???
  }

  def search(query: String): Future[Seq[Movie]] = driver.readSession { session =>
    c"""MATCH (movie:Movie)
        WHERE lower(movie.title) CONTAINS ${query.toLowerCase}
        RETURN movie""".query[Movie].list(session)
  }


}
