/**
 * Movie Score Parser
 * From JSON movies data, create Neo4j database with nodes
 * and relationships between Movies, Peoples and Genres
 * Jeremy Favre & Steven Liatti
 */

package ch.hepia

import java.util.concurrent.TimeUnit

import ch.hepia.Domain._
import neotypes.implicits._
import org.neo4j.driver.v1.{AuthTokens, Config, GraphDatabase}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * Program entry point
 */
object Main {
  def main(args: Array[String]): Unit = {
    val actorsByMovie = if (args.length > 0) args(0).toInt else 30
    println(actorsByMovie)
    val jobsForMovie = List("Director", "Writer", "Screenplay", "Producer",
      "Director of Photography", "Editor", "Composer", "Special Effects")
    val movieMakerScoreDivisor = 2.0

    val loadConfig = LoadConfig.load()
    val config = Config.build()
      .withEncryption()
      .withConnectionTimeout(60, TimeUnit.SECONDS)
      .withMaxConnectionLifetime(2, TimeUnit.HOURS)
      .withMaxConnectionPoolSize(200)
      .withConnectionAcquisitionTimeout(10, TimeUnit.HOURS)
      .toConfig
    val driver = GraphDatabase.driver(
      loadConfig.database.url,
      AuthTokens.basic(loadConfig.database.username, loadConfig.database.password),
      config
    )

    val movieService = new MovieService(driver.asScala[Future])
    println("Create constraints")
    movieService.createConstraints()

    println("Read movies in JSON and make filters")
    val rMovies = movieService.readMoviesFromFile("data/movies.json").toList
    val movies = rMovies.map(m => {
      val actors = m.credits.cast.filter(a => a.order < actorsByMovie)
      val movieMakers = m.credits.crew.filter(c => jobsForMovie.contains(c.job))
      val credits = Credits(actors, movieMakers)
      Movie(m.id, m.title, m.budget, m.revenue, m.genres, credits, m.similar, m.recommendations)
    })

    val peoplesMap = mutable.Map[Long, (SimplePeople, mutable.Set[String])]()
    val genresSet = mutable.Set[Genre]()
    val moviesForPeople = mutable.Map[Long, List[MovieForPeople]]()
    val genresForPeople = mutable.Map[Long, List[GenreForPeople]]()
    val peopleToPeople = mutable.Map[Long, List[Long]]()

    def processPeople(p: People, m: Movie, genres: List[Genre]): Unit = {
      val (label, score) = p match {
        case actor: Actor => ("Actor", movieService.computeMovieScore(m) / (actor.order + 1).toDouble)
        case _ => ("MovieMaker", movieService.computeMovieScore(m) / movieMakerScoreDivisor)
      }

      if (peoplesMap.contains(p.id)) peoplesMap(p.id)._2 += label
      else {
        val sp = SimplePeople(p.id, p.name, p.intToGender())
        val set = mutable.Set(label)
        peoplesMap.put(p.id, (sp, set))
      }

      val mfp = MovieForPeople(m.id, p, score)
      if (moviesForPeople.contains(p.id)) moviesForPeople.put(p.id, mfp :: moviesForPeople(p.id))
      else moviesForPeople.put(p.id, mfp :: Nil)

      genres.foreach { g =>
        val gfp = GenreForPeople(g.id, p)
        if (genresForPeople.contains(p.id)) genresForPeople.put(p.id, gfp :: genresForPeople(p.id))
        else genresForPeople.put(p.id, gfp :: Nil)
      }
    }

    println("Create maps from each movies")
    movies.foreach { m =>
      val actors = m.credits.cast
      val movieMakers = m.credits.crew
      val genres = m.genres

      genres.foreach(g => genresSet.add(g))
      actors.foreach(a => processPeople(a, m, genres))
      movieMakers.foreach(mm => processPeople(mm, m, genres))

      // knowsPeopleRelation
      val people = actors ::: movieMakers
      for {
        p1 <- people
        p2 <- people
      } yield {
        if (p1.id != p2.id) {
          if (peopleToPeople.contains(p1.id)) peopleToPeople.put(p1.id, p2.id :: peopleToPeople(p1.id))
          else peopleToPeople.put(p1.id, p2.id :: Nil)
        }
      }
    }

    println("Start to add movies, genres and peoples nodes")
    val moviesInsertions = Future.sequence(movies.map(m => movieService.addMovie(m)))
    val genresInsertions = Future.sequence(genresSet.map(g => movieService.addGenres(g)))
    val peoplesInsertions = Future.sequence(peoplesMap.map { case (id, (sp, set)) =>
      val score = moviesForPeople(id).map(mfp => mfp.score).sum / moviesForPeople(id).length
      movieService.addPeople(sp, score, set)
    })

    Await.result(moviesInsertions, Duration.Inf)
    Await.result(genresInsertions, Duration.Inf)
    Await.result(peoplesInsertions, Duration.Inf)

    println("Start to make relations between all nodes")
    // Second step : add similar and recommended movies
    // Add similar movies for each movie
    val similar = for {
      m1 <- movies
      m2 <- m1.similar.getOrElse(Similar(Nil)).results
    } yield movieService.addSimilarRelation(m1, m2)

    // Add recommended movies for each movie
    val recommendations = for {
      m1 <- movies
      m2 <- m1.recommendations.getOrElse(Recommendations(Nil)).results
    } yield movieService.addRecommendationsRelation(m1, m2)

    val moviesGenres = for {
      m <- movies
      g <- m.genres
    } yield movieService.addMoviesGenres(m, g)

    // Add addInMoviesRelation
    val peoplesMovies = for {
      (_, moviesList) <- moviesForPeople
      m <- moviesList
    } yield movieService.addInMoviesRelation(m.people, m.movieId)

    def genresPeopleCount(gfpList: List[GenreForPeople], kind: String): Map[Long, (People, Int)] = {
      val knownFor = gfpList.filter(gfp => gfp.people.getClass.toString == kind)
      println(gfpList, kind, knownFor)
      val genresFor = knownFor.groupBy(gfp => gfp.genreId)
        .map { case (l, peoples) => (l, peoples.map(p => p.people))}
      genresFor
        .map { case (l, peoples) => (l, (peoples.head, peoples.length)) }
    }

    // TODO: ugly -> resolve
    val knownForRelations = genresForPeople.map { case (peopleId, gfpList) =>
      val genresActingCount = genresPeopleCount(gfpList, "class ch.hepia.Domain$Actor")
      val genresWorkingCount = genresPeopleCount(gfpList, "class ch.hepia.Domain$MovieMaker")
      (peopleId, (genresActingCount, genresWorkingCount))
    }

    val peoplesGenresActing = for {
      (_, (genresActingCount, _)) <- knownForRelations
      (genreId, (people, count)) <- genresActingCount
    } yield {
      println(s"Acting, $genreId, $people, $count")
      movieService.addKnownForRelation(people, genreId, count)
    }

    val peoplesGenresWorking = for {
      (_, (_, genresWorkingCount)) <- knownForRelations
      (genreId, (people, count)) <- genresWorkingCount
    } yield {
      println(s"Working, $genreId, $people, $count")
      movieService.addKnownForRelation(people, genreId, count)
    }

    val knows = peopleToPeople.map { case (peopleId, pIds) =>
      val friendIdWithCount = pIds.groupBy(i => i)
        .map { case (l, longs) => (l, longs.length) }
      (peopleId, friendIdWithCount)
    }

    val knowsPeopleRelation = for {
      (p1, p2Count) <- knows
      (p2, count) <- p2Count
    } yield movieService.addKnowsRelation(p1, p2, count)

    val fSimilar = Future.sequence(similar)
    val fRecommendations = Future.sequence(recommendations)
    val fMoviesGenres = Future.sequence(moviesGenres)

    val fPeoplesMovies = Future.sequence(peoplesMovies)
    val fPeoplesGenresActing = Future.sequence(peoplesGenresActing)
    val fPeoplesGenresWorking = Future.sequence(peoplesGenresWorking)
    val fKnowsPeopleRelation = Future.sequence(knowsPeopleRelation)

    Await.result(fSimilar, Duration.Inf)
    println("Similar relations for movies added")
    Await.result(fRecommendations, Duration.Inf)
    println("Recommended relations for movies added")
    Await.result(fMoviesGenres, Duration.Inf)
    println("Genres relations for movies added")

    Await.result(fPeoplesMovies, Duration.Inf)
    println("Movies relations for peoples added")
    Await.result(fPeoplesGenresActing, Duration.Inf)
    println("Genres acting relations for peoples added")
    Await.result(fPeoplesGenresWorking, Duration.Inf)
    println("Genres working relations for peoples added")
    Await.result(fKnowsPeopleRelation, Duration.Inf)
    println("Knows relations for peoples added")

    val algorithmService = new AlgorithmService(driver.asScala[Future])
    Await.result(algorithmService.pagerank(), Duration.Inf)
    Await.result(algorithmService.centrality(), Duration.Inf)
    Await.result(algorithmService.genreDegree(), Duration.Inf)
    Await.result(algorithmService.communities(), Duration.Inf)
    Await.result(algorithmService.similarities(), Duration.Inf)
    println("Fourth step done, compute some algorithms")

    driver.close()
    println("End of main")
  }
}
