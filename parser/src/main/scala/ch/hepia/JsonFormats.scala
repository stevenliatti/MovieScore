package ch.hepia


import ch.hepia.Domain.{Cast, Credits, Crew, Genre, Movie}
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit val genreFormat = jsonFormat2(Genre)
  implicit val actorFormat = jsonFormat5(Cast)
  implicit val crewFormat = jsonFormat4(Crew)
  implicit val creditFormat = jsonFormat2(Credits)
  implicit val movieFormat = jsonFormat6(Movie)
}
