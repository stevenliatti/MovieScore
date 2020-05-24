var viz;

const URL_DB = "bolt://localhost:7687";
const USER = "neo4j";
const PWD = "wem2020";
const INITIAL_QUERY = "MATCH p=(:Genre)<-[:BELONGS_TO|:KNOWN_FOR_ACTING|:KNOWN_FOR_WORKING]-() RETURN p LIMIT 50";

const driver = neo4j.v1.driver(
    'bolt://localhost',
    neo4j.v1.auth.basic(USER, PWD)
)

const session = driver.session()

function draw() {
    var config = {
        container_id: "viz",
        server_url: URL_DB,
        server_user: USER,
        server_password: PWD,
        labels: {
            "Movie": {
                "caption": "title",
                "size": "score"
            },
            "People": {
                "caption": "name",
                "community": "Black"
            },
            "Actor": {
                "caption": "name"
            },
            "MovieMaker": {
                "caption": "name"
            },
            "Genre": {
                "caption": "name"
            }
        },
        relationships: {
            "BELONGS_TO": {
                "caption": false
            },
            "KNOWN_FOR_ACTING": {
                "caption": false,
                "thickness": "count"
            },
            "KNOWN_FOR_WORKING": {
                "caption": false,
                "thickness": "count"
            },
            "KNOWS": {
                "caption": false,
                "thickness": "count"
            },
            "PLAY_IN": {
                "caption": false
            },
            "WORK_IN": {
                "caption": false
            },
            "RECOMMENDATIONS": {
                "caption": false
            },
            "SIMILAR": {
                "caption": false
            }

        },
        initial_cypher: INITIAL_QUERY
    };

    viz = new NeoVis.default(config);
    viz.render();
}

function updateSearchBar(query) {
    document.getElementById("searchBar").value = query;
}

function execQuery() {
    const q = document.getElementById("searchBar").value;
    viz.renderWithCypher(q);
}

function onSearchQuery() {
    document.getElementById("searchBar").focus();
}

function onSearchMovie() {
    const title = document.getElementById("searchMovie").value;
    var relations = '';
    if (document.getElementById("rdGenre").checked)
        relations += ':BELONGS_TO|';
    if(document.getElementById("rdPeople").checked)
        relations += ':PLAY_IN|:WORK_IN|';
    if(document.getElementById("rdRecommendations").checked)
        relations += ':RECOMMENDATIONS|';
    if(document.getElementById("rbSimilar").checked)
        relations += ':SIMILAR|';
    relations = relations.slice(0, -1);
    if(relations !== '')
        var q = `MATCH p=(m: Movie{title: "${title}"})-[${relations}]-() RETURN p`;
    else
        var q = `MATCH (m:Movie {title: "${title}"}) RETURN m`;
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function onSearchPeople() {
    const name = document.getElementById("searchPeople").value;
    var relations = '';
    if (document.getElementById("rdMovies").checked)
        relations += ":KNOWN_FOR_ACTING|:KNOWN_FOR_WORKING|";
    if(document.getElementById("rdPeopleKnown").checked)
        relations += ":KNOWS|";
    relations = relations.slice(0, -1);
    if(relations !== '')
        var q = `MATCH res=(p: People {name: "${name}"})-[${relations}]-() RETURN res`;
    else
        var q = `MATCH (n:People {name: "${name}"}) RETURN n`;
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function onSearchGenre() {
    const name = document.getElementById("searchGenre").value;
    var relations = '';
    if (document.getElementById("rdMoviesAssociated").checked)
        relations += `:BELONGS_TO|`;
    if(document.getElementById("rdPeopleAssociated").checked)
        relations += `:KNOWN_FOR_ACTING|:KNOWN_FOR_WORKING|`;
    relations = relations.slice(0, -1);
    if(relations !== '')
        var q = `MATCH g=(:Genre {name: "${name}"})<-[${relations}]-() RETURN g LIMIT 30`;
    else
        var q = `MATCH (n:Genre {name: "${name}"}) RETURN n`;
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function initialQuery() {
    viz.renderWithCypher(INITIAL_QUERY);
    updateSearchBar(INITIAL_QUERY);
}

/*
 * Neo4j request
 */
function all_movies() {
    const q = "MATCH (n:Movie) RETURN n LIMIT 100";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function movie_genre() {
    const q = "MATCH p=()-[r:BELONGS_TO]->() RETURN p LIMIT 50";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function movie_people_playin() {
    const q = "MATCH p=()-[r:PLAY_IN]->() RETURN p LIMIT 50";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function movie_people_workin() {
    const q = "MATCH p=()-[r:WORK_IN]->() RETURN p LIMIT 50";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function all_people() {
    const q = "MATCH (p:People) RETURN p LIMIT 500";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function knowledge_people() {
    const q = "MATCH p=()-[r:KNOWS]->() RETURN p LIMIT 100";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function people_knownFor() {
    const q = "MATCH r=(a: Actor)-->(g: Genre)<--(mm: MovieMaker) RETURN r LIMIT 200";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function all_genres() {
    const q = "MATCH (n:Genre) RETURN n LIMIT 100";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function genres_movies() {
    const q = ""; //TODO:
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function genres_peoples() {
    const q = ""; //TODO:
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

/*
 * Algos
 */
function page_rank() {
    const q = ""; //TODO:
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function shortest_path() {
    const p1 = document.getElementById("SPP1").value;
    const p2 = document.getElementById("SPP2").value;
    const q = `MATCH (p1:People { name: '${p1}'}),(p2:People {name: '${p2}' }), p = shortestPath((p1)-[r:KNOWS *]-(p2)) RETURN p`;
    updateSearchBar(q);
    viz.renderWithCypher(q);
}


/*
 * User to side navbar
 */
$(document).ready(function () {
    $('#sidebarCollapse').on('click', function () {
        $('#sidebar').toggleClass('active');
        $(this).toggleClass('active');
    });
});

/*
 * Gestion of autocompletion
 */
$('.basicAutoSelectPeople').autoComplete({
    resolver: 'custom',
    events: {
        search: function (query, callback) {
            session
                .run(`MATCH (p:People) WHERE LOWER(p.name) STARTS WITH LOWER("${query}") RETURN p.name`)
                .then(res => {
                    console.log(res);
                    const records = Array.from(res.records);
                    const names = records.map(r => r._fields[0]).sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()));
                    console.log(names);
                    callback(names);
                });
        }
    }
});

$('.basicAutoSelectMovie').autoComplete({
    resolver: 'custom',
    events: {
        search: function (query, callback) {
            session
                .run(`MATCH (m:Movie) WHERE LOWER(m.title) STARTS WITH LOWER("${query}") RETURN m.title`)
                .then(res => {
                    const records = Array.from(res.records);
                    const names = records.map(r => r._fields[0]).sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()));
                    callback(names);
                });
        }
    }
});

$('.basicAutoSelectGenre').autoComplete({
    resolver: 'custom',
    events: {
        search: function (query, callback) {
            session
                .run(`MATCH (g:Genre) WHERE LOWER(g.name) STARTS WITH LOWER("${query}") RETURN g.name`)
                .then(res => {
                    const records = Array.from(res.records);
                    const names = records.map(r => r._fields[0]).sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()));
                    callback(names);
                });
        }
    }
});
