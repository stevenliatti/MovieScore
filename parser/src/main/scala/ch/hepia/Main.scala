/**
 * Movie Score Parser
 * From JSON movies data, create Neo4j database with nodes
 * and relationships between Movies, Peoples and Genres
 * Jeremy Favre & Steven Liatti
 */

package ch.hepia

import java.util.concurrent.TimeUnit

import ch.hepia.Domain.{Recommendations, Similar}
import neotypes.implicits._
import org.neo4j.driver.v1.{AuthTokens, Config, GraphDatabase}

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

    val movies = movieService.readMoviesFromFile("data/movies.json").toList

    // First step : foreach movie, add it, with associated genres and peoples
    // and foreach people, link it to other peoples and genres
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
        p <- people
        genre <- m.genres
      } yield movieService.addKnownForRelation(p, genre)

      // knowsPeopleRelation
      for {
        p1 <- people
        p2 <- people
      } yield if(p1.id != p2.id) movieService.addKnowsRelation(p1, p2)
    })

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

    // TODO: improve allPeople creation
    // Last step : compute final people score
    val allPeople = movies.flatMap(
      m => m.credits.cast.filter(a => a.order < actorsByMovie) :::
        m.credits.crew.filter(c => jobsForMovie.contains(c.job))
    ).groupBy(p => p.id).map(idToList => idToList._2.head).toList
    val finalPeopleScore = allPeople.map(p => movieService.computeFinalPeopleScore(p))

    val fSimilar = Future.sequence(similar)
    val fRecommendations = Future.sequence(recommendations)
    val fFinalPeopleScore = Future.sequence(finalPeopleScore)

    Await.result(fSimilar, Duration.Inf)
    Await.result(fRecommendations, Duration.Inf)
    Await.result(fFinalPeopleScore, Duration.Inf)

    Thread.sleep(3000) // TODO : 2546 noeuds 38435 relations
    driver.close()
  }
}

// Requêtes utiles :
/*
Pour choper les relations d'un movie: MATCH p=()-[]->(m: Movie {id: 22})-[b: BELONGS_TO]->() RETURN p, b LIMIT 20
Relation entre 2 people : MATCH (p2: People {name: 'William Lustig'})<-[r:KNOWS]-(p1: People {name: 'John Landis'}) RETURN p1, p2
TODO: map entre people et liste de job/personnages pour avoir PLAY_IN / WORK_IN comme tableau
TODO: améliorer division du score des movie makers


// -----------------------------------------------------------------
// CONTRAINTES
TODO: créer contraintes (+ index gratuit) comme ceci
CREATE CONSTRAINT ON (m:Movie) ASSERT m.id IS UNIQUE;
CREATE CONSTRAINT ON (g:Genre) ASSERT g.id IS UNIQUE;
CREATE CONSTRAINT ON (p:People) ASSERT p.id IS UNIQUE;

// créer index
CREATE INDEX ON :Movie(title);

// -----------------------------------------------------------------
// PAGERANK
// créer graphe pour exécuter les algos

// movies similar pagerank en mode "debug"
// CALL gds.graph.create.cypher(
//     'pagerank-movie-similar',
//     'MATCH (m:Movie) RETURN id(m) AS id',
//     'MATCH (a:Movie)-[:SIMILAR]->(b:Movie) RETURN id(a) AS source, id(b) AS target'
// ) YIELD graphName, nodeCount, relationshipCount, createMillis;

// movies similar pagerank en mode prod
CALL gds.graph.create(
    'pagerank-movie-similar',
    'Movie',
    'SIMILAR'
);

// execute pagerank on similar movies
CALL gds.pageRank.write('pagerank-movie-similar', {
  maxIterations: 20,
  dampingFactor: 0.85,
  writeProperty: 'pagerankSimilar'
}) YIELD nodePropertiesWritten AS writtenProperties, ranIterations;


// movies recommendations pagerank en mode prod
CALL gds.graph.create(
    'pagerank-movie-recommendations',
    'Movie',
    'RECOMMENDATIONS'
);

// execute pagerank on recommendations movies
CALL gds.pageRank.write('pagerank-movie-recommendations', {
  maxIterations: 20,
  dampingFactor: 0.85,
  writeProperty: 'pagerankRecommendations'
}) YIELD nodePropertiesWritten AS writtenProperties, ranIterations;


// show movies by pageRank recommendations
// MATCH (m:Movie) 
// RETURN DISTINCT m.title, m.pagerankRecommendations, m.pagerankSimilar
// ORDER BY m.pagerankRecommendations DESC LIMIT 50;


// -----------------------------------------------------------------
// CENTRALITY

// calcul le Degree Centrality pour les People avec KNOWS
CALL gds.alpha.degree.write({
  nodeProjection: 'People',
  relationshipProjection: {
    KNOWS: {
      type: 'KNOWS',
      projection: 'REVERSE'
    }
  },
  writeProperty: 'knowsDegree'
});

// -----------------------------------------------------------------
// GENRE DEGREE

// ajout de propriétés de taille aux Genre
MATCH (g:Genre) SET g.belongsToDegree = size( (g)<-[:BELONGS_TO]-() );
MATCH (g:Genre) SET g.knownForActingDegree = size( (g)<-[:KNOWN_FOR_ACTING]-() );
MATCH (g:Genre) SET g.knownForWorkingDegree = size( (g)<-[:KNOWN_FOR_WORKING]-() );
MATCH (g:Genre) SET g.knownForDegree = size( (g)<-[:KNOWN_FOR_WORKING|:KNOWN_FOR_ACTING]-() );
MATCH (g:Genre) SET g.degree = size( (g)<-[:BELONGS_TO|:KNOWN_FOR_WORKING|:KNOWN_FOR_ACTING]-() );


// -----------------------------------------------------------------
// COMMUNITY

CALL gds.graph.create(
    'people-knows-community-louvain-graph',
    'People',
    {
        KNOWS: {
            orientation: 'UNDIRECTED'
        }
    },
    {
        relationshipProperties: 'count'
    }
);

CALL gds.louvain.write('people-knows-community-louvain-graph', { writeProperty: 'knowsCommunity' })
YIELD communityCount, modularity, modularities;


// -----------------------------------------------------------------
// SIMILARITY MOVIES

// create
CALL gds.graph.create('movie-belongs-to-node-similar', ['Movie', 'Genre'], 'BELONGS_TO');

// execute
CALL gds.nodeSimilarity.write('movie-belongs-to-node-similar', {
    writeRelationshipType: 'SIMILAR_JACCARD',
    writeProperty: 'score'
}) YIELD nodesCompared, relationshipsWritten;

// -----------------------------------------------------------------
// SIMILARITY PEOPLES

// create
CALL gds.graph.create('people-known-for-acting-node-similar', ['People', 'Genre'], 'KNOWN_FOR_ACTING');

// execute
CALL gds.nodeSimilarity.write('people-known-for-acting-node-similar', {
    writeRelationshipType: 'SIMILAR_FOR_ACTING',
    writeProperty: 'score'
}) YIELD nodesCompared, relationshipsWritten;


// create
CALL gds.graph.create('people-known-for-working-node-similar', ['People', 'Genre'], 'KNOWN_FOR_WORKING');

// execute
CALL gds.nodeSimilarity.write('people-known-for-working-node-similar', {
    writeRelationshipType: 'SIMILAR_FOR_WORKING',
    writeProperty: 'score'
}) YIELD nodesCompared, relationshipsWritten;

 */