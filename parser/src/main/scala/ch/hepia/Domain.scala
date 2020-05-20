package ch.hepia

object Domain {
  def intToGender(i: Int) = i match {
    case 1 => "Female"
    case 2 => "Male"
    case _ => "Undefined"
  }

  trait People {
    def id: Int
    def name: String
    def gender: Int
  }

  case class Genre(id: Int, name: String)
  case class Actor(id: Int, name: String, gender: Int, order: Int, character: String) extends People
  case class MovieMaker(id: Int, name: String, gender: Int, job: String) extends People
  case class Credits(cast: List[Actor], crew: List[MovieMaker])
  case class MovieId(id: Int)
  case class Similar(results: List[MovieId])
  case class Recommendations(results: List[MovieId])
  case class Movie(
                    id: Int,
                    title: String,
                    budget: Int,
                    revenue: Int,
                    genres: List[Genre],
                    credits: Credits,
                    similar: Similar,
                    recommendations: Recommendations
                  )
}
