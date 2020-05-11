package ch.hepia


import ch.hepia.Domain.{Actor, Credits, MovieMaker, Genre, Movie}
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit val genreFormat = jsonFormat2(Genre)
  implicit val actorFormat = jsonFormat5(Actor)
  implicit val crewFormat = jsonFormat4(MovieMaker)
  implicit val creditFormat = jsonFormat2(Credits)
  implicit val movieFormat = jsonFormat6(Movie)
}
