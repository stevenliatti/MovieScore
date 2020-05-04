package ch.hepia

import spray.json._

import scala.io.Source

object Main extends App {
  case class Genre(id: Int, name: String)
  case class Cast(id: Int, name: String, gender: Int, character: String)
  case class Crew(id: Int, name: String, gender: Int, job: String)
  case class Credits(cast: List[Cast], crew: List[Crew])
  case class Movie(id: Int, title: String, budget: Int, revenue: Int, genres: List[Genre], credits: Credits)

  object JsonFormats extends DefaultJsonProtocol {
    implicit val genreFormat = jsonFormat2(Genre)
    implicit val actorFormat = jsonFormat4(Cast)
    implicit val crewFormat = jsonFormat4(Crew)
    implicit val creditFormat = jsonFormat2(Credits)
    implicit val movieFormat = jsonFormat6(Movie)
  }

  import JsonFormats._

  val movies = Source.fromFile("data/movies.json").getLines
    .map(line => JsonParser(line).convertTo[Movie])
  movies.foreach(println)
}
