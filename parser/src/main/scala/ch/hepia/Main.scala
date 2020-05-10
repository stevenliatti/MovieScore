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
  import JsonFormats._

  val config = Config.load()
  val driver = GraphDatabase.driver(config.database.url, AuthTokens.basic(config.database.username, config.database.password))

  val movieService = new MovieService(driver.asScala[Future])


  val movies = Source.fromFile("data/movies.json").getLines
    .map(line => JsonParser(line).convertTo[Movie])

  movies.foreach(m => movieService.insertMovie(m))
  // val r = movieService.search("Slackers")
  // r.map(list => list.foreach(println))
  /*val res = Await.result(r, Duration.Inf)
  println(res)*/
  Thread.sleep(10000)

  driver.close
}
