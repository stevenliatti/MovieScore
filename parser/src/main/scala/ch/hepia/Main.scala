package ch.hepia

import ch.hepia.Domain.Movie
import org.neo4j.driver.v1.{AuthTokens, GraphDatabase}
import neotypes.implicits._
import spray.json._

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main extends App {

  val config = Config.load()
  val driver = GraphDatabase.driver(config.database.url, AuthTokens.basic(config.database.username, config.database.password))
  val movieService = new MovieService(driver.asScala[Future])

  val actorsByMovie = 30
  val jobsForMovie = List("Director", "Writer", "Screenplay", "Producer",
    "Director of Photography", "Editor", "Composer", "Special Effects")
  val movies = movieService.readMoviesFromFile("data/movies.json")

  movies.foreach(m => {
    val f = movieService.insertMovie(m)
    Await.result(f, Duration.Inf)
    m.genres.foreach(g => {
      val r = movieService.insertGenres(g, m)
      Await.result(r, Duration.Inf)
    })

    // Insert actors
    val actors = m.credits.cast.filter(a => a.order < actorsByMovie)
    actors.foreach(a => {
      val s = movieService.insertActor(a, m)
      Await.result(s, Duration.Inf)
    })

    // Insert movieMakers
    val movieMakers = m.credits.crew.filter(c => jobsForMovie.contains(c.job))
    movieMakers.foreach(mm => {
      val f = movieService.insertMovieMaker(mm, m)
      Await.result(f, Duration.Inf)
    })
  })
  // val r = movieService.search("Slackers")
  // r.map(list => list.foreach(println))
  Thread.sleep(3000)
  driver.close
}
