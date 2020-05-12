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

    // Insert actors movieMakers
    val actors = m.credits.cast.filter(a => a.order < actorsByMovie)
    val movieMakers = m.credits.crew.filter(c => jobsForMovie.contains(c.job))
    val people = actors ::: movieMakers
    people.foreach(p => {
      val f = movieService.insertPeople(p, m)
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
      println(s"P1 : $p1 // p2 : $p2")
      movieService.knowsPeopleRelation(p1, p2)
    }

  })
  // val r = movieService.search("Slackers")
  // r.map(list => list.foreach(println))
  Thread.sleep(3000) // TODO : 2546 noeuds 38435 relations
  driver.close
}

// Requêtes utiles :
/*
Pour choper les relations d'un movie: MATCH p=()-[]->(m: Movie {id: 22})-[b: BELONGS_TO]->() RETURN p, b LIMIT 20
Relation entre 2 people : MATCH (p2: People {name: 'William Lustig'})<-[r:KNOWS]-(p1: People {name: 'John Landis'}) RETURN p1, p2
TODO : Voir pour les peoples bidirectionnel
TODO : Calculer le score du people
TODO : Voir pour le pb concurence
 */