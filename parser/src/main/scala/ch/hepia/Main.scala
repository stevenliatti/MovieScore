/**
 * Movie Score Parser
 * From JSON movies data, create Neo4j database with nodes
 * and relationships between Movies, Peoples and Genres
 * Jeremy Favre & Steven Liatti
 */

package ch.hepia

import ch.hepia.Domain.{Recommendations, Similar}
import neotypes.implicits._
import org.neo4j.driver.v1.{AuthTokens, GraphDatabase}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * Program entry point
 */
object Main extends App {

  val config = Config.load()
  val driver = GraphDatabase.driver(config.database.url, AuthTokens.basic(config.database.username, config.database.password))
  val movieService = new MovieService(driver.asScala[Future])

  val actorsByMovie = 30
  val jobsForMovie = List("Director", "Writer", "Screenplay", "Producer",
    "Director of Photography", "Editor", "Composer", "Special Effects")
  val movies = movieService.readMoviesFromFile("data/movies.json").toList

  movies.foreach(m => {
    val f = movieService.addMovie(m)
    Await.result(f, Duration.Inf)
    m.genres.foreach(g => {
      val r = movieService.addGenres(g, m)
      Await.result(r, Duration.Inf)
    })

    // Insert actors movieMakers
    val actors = m.credits.cast.filter(a => a.order < actorsByMovie)
    val movieMakers = m.credits.crew.filter(c => jobsForMovie.contains(c.job))
    val people = actors ::: movieMakers
    people.foreach(p => {
      val f = movieService.addPeople(p, m)
      Await.result(f, Duration.Inf)
    })

    // Add addKnownForRelation
    for {
      people <- people
      genre <- m.genres
    } yield movieService.addKnownForRelation(people, genre)

    // knowsPeopleRelation
    for {
      p1 <- people
      p2 <- people
    } yield if(p1.id != p2.id) {
      println(s"p1 : $p1, p2 : $p2")
      movieService.addKnowsRelation(p1, p2)
    }

  })

  // Add similar movies for each movie
  val similar = for {
    m1 <- movies
    m2 <- m1.similar.getOrElse(Similar(Nil)).results
  } yield {
    println(s"SIMILAR: Movie.id(${m1.id}), $m2")
    movieService.addSimilarRelation(m1, m2)
  }

  // Add recommended movies for each movie
  val recommendations = for {
    m1 <- movies
    m2 <- m1.recommendations.getOrElse(Recommendations(Nil)).results
  } yield {
    println(s"RECOMMENDATIONS: Movie.id(${m1.id}), $m2")
    movieService.addRecommendationsRelation(m1, m2)
  }

  val fSimilar = Future.sequence(similar)
  val fRecommendations = Future.sequence(recommendations)

  Await.result(fSimilar, Duration.Inf)
  Await.result(fRecommendations, Duration.Inf)

  Thread.sleep(3000) // TODO : 2546 noeuds 38435 relations
  driver.close()
}

// Requêtes utiles :
/*
Pour choper les relations d'un movie: MATCH p=()-[]->(m: Movie {id: 22})-[b: BELONGS_TO]->() RETURN p, b LIMIT 20
Relation entre 2 people : MATCH (p2: People {name: 'William Lustig'})<-[r:KNOWS]-(p1: People {name: 'John Landis'}) RETURN p1, p2
TODO : Calculer le score du people
TODO : map entre people et liste de job/personnages pour avoir PLAY_IN / WORK_IN comme tableau
TODO : Voir pour le pb concurence
 */