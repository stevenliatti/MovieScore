package ch.hepia

object Domain {
  case class Genre(id: Int, name: String)
  case class Cast(id: Int, name: String, gender: Int, order: Int, character: String)
  case class Crew(id: Int, name: String, gender: Int, job: String)
  case class Credits(cast: List[Cast], crew: List[Crew])
  case class Movie(id: Int, title: String, budget: Int, revenue: Int, genres: List[Genre], credits: Credits)
}
