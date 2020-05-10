package ch.hepia

import ch.hepia.Domain.{Credits, Genre, Movie}
import neotypes.Driver
import neotypes.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MovieService(driver: Driver[Future]) {

  def insertMovie(movie: Movie): Future[Unit] = driver.readSession { session =>

    val score: Double = movie.revenue.toDouble / movie.budget.toDouble

    // (p:Person {name: "Jennifer"})-[rel:LIKES]->(g:Technology {type: "Graphs"}) TODO: regarder cette query pour relation avec genres



    c"""CREATE (movie: Movie {
        id: ${movie.id},
        title: ${movie.title},
        budget:${movie.budget},
        revenue:${movie.revenue},
        score: $score
     })->(genre: Genre)""".query[Unit].execute(session)

    // insertGenres(movie.genres)
    // insertCredits(movie.credits)
  }

  private def insertGenres(genres: List[Genre]): Future[Unit] = driver.readSession { session =>
    ???
    /*genres.foreach(g => TODO: GERER TYPE DE RETOUR
      c"""CREATE (genre: Genre {
        id: ${g.id},
        name: ${g.name}})""".query[Unit].execute(session))*/
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
