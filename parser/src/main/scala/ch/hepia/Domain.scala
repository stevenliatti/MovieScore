package ch.hepia

object Domain {
  def intToGender(i: Int) = i match {
    case 1 => "Female"
    case 2 => "Male"
    case _ => "Undefined"
  }

  case class Genre(id: Int, name: String)
  case class Actor(id: Int, name: String, gender: Int, order: Int, character: String)
  case class MovieMaker(id: Int, name: String, gender: Int, job: String)
  case class Credits(cast: List[Actor], crew: List[MovieMaker])
  case class Movie(id: Int, title: String, budget: Int, revenue: Int, genres: List[Genre], credits: Credits)
}
