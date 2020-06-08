var viz;

const INITIAL_QUERY = "MATCH r=(p:People)-->(m:Movie)-->(g:Genre) RETURN r ORDER BY m.score LIMIT 500";
const TMDB_URL = "https://www.themoviedb.org/"

const driver = neo4j.v1.driver(
    env.URL_DB,
    neo4j.v1.auth.basic(env.USER, env.PWD)
)

const session = driver.session()

function defineConfig() {
    let sizeMovie = document.querySelector('input[name="MovieAlgoChoice"]:checked').value;
    let sizePeople = document.querySelector('input[name="PeopleAlgoChoice"]:checked').value;
    let sizeGenre = document.querySelector('input[name="GenreAlgoChoice"]:checked').value;
    if(document.getElementById("searchBar").value !== '') // En cas de reload recup la query active
        INITIAL_QUERY = document.getElementById("searchBar").value;

    var config = {
        container_id: "viz",
        server_url: env.URL_DB,
        server_user: env.USER,
        server_password: env.PWD,
        labels: {
            "Movie": {
                "caption": "title",
                "size": sizeMovie,
            },
            "People": {
                "caption": "name",
                "size": sizePeople,
                "community": "knowsCommunity"
            },
            "Actor": {
                "caption": "name"
            },
            "MovieMaker": {
                "caption": "name"
            },
            "Genre": {
                "caption": "name",
                "size": sizeGenre
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
            },
            "SIMILAR_MOVIES_ALGO": {
                "caption": false,
                "thickness": "score"
            },
            "SIMILAR_FOR_ACTING": {
                "caption": false,
                "thickness": "score"
            },
            "SIMILAR_FOR_WORKING": {
                "caption": false,
                "thickness": "score"
            }
        },
        initial_cypher: INITIAL_QUERY
    };

    return config;
}


function draw(config) {
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
    if(document.getElementById("rbSimilarJaccard").checked)
        relations += ':SIMILAR_MOVIES_ALGO|';
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
    if(document.getElementById("rdSimilarForActing").checked)
        relations += ":SIMILAR_FOR_ACTING|";
    if(document.getElementById("rdSimilarForWorking").checked)
        relations += ":SIMILAR_FOR_WORKING|";
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
function bestMovies() {
    const q = "MATCH r=()-->(m:Movie)-->(g:Genre) RETURN r ORDER BY m.score DESC LIMIT 200";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function movieGenre() {
    const q = "MATCH p=(m)-[r:BELONGS_TO]->() RETURN p ORDER BY m.score DESC LIMIT 100";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function moviePeoplePlayIn() {
    const q = "MATCH r=(p)-[:PLAY_IN]->(m) RETURN r ORDER BY m.score DESC LIMIT 100";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function moviePeopleWorkIn() {
    const q = "MATCH r=(p)-[:WORK_IN]->(m) RETURN r ORDER BY m.score DESC LIMIT 100";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function movieSimilar() {
    const q = "MATCH r=(m1)-[:SIMILAR]->(m2) RETURN r ORDER BY m1.score DESC LIMIT 100";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function movieRecommendations() {
    const q = "MATCH r=(m1)-[:RECOMMENDATIONS]->(m2) RETURN r ORDER BY m1.score DESC LIMIT 100";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function bestPeoples() {
    const q = "MATCH r=(p:People)-->() RETURN r ORDER BY p.score DESC LIMIT 200";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function knowledgePeople() {
    const q = "MATCH r=(p:People)-[:KNOWS]->() RETURN r ORDER BY p.knowsDegree DESC LIMIT 100";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function peopleKnownFor() {
    const q = "MATCH r=(a: Actor)-->(g: Genre)<--(mm: MovieMaker) RETURN r ORDER BY a.knowsDegree DESC LIMIT 200";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function allGenres() {
    const q = "MATCH (n:Genre) RETURN n ORDER BY n.degree DESC LIMIT 100";
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

/*
 * Textual requests
 */

function bestNodes(cypherQuery, firstKey, secondKey, tmdbType) {
    session
        .run(cypherQuery)
        .then(res => {
            const records = Array.from(res.records);
            const nodes = records.map(r => {
                return {
                    title: r._fields[1],
                    score: r._fields[2],
                    linkTMDB: TMDB_URL + tmdbType + '/' + r._fields[0]
                };
            });
            clearModalTableContent();
            defineTableHeader(firstKey, secondKey);
            nodes.forEach(m => {
                let title = document.createElement("a");
                let score = document.createTextNode(Math.round(m.score * 100) / 100);
                title.appendChild(document.createTextNode(m.title));
                title.href = m.linkTMDB;
                title.title = `Show ${tmdbType} on TMDb`;
                title.style.color = 'blue';
                title.target = "_blank"
                defineTableBody(title, score);
            });
        });
}

function bestScoreMovies() {
    bestNodes(
        `MATCH (m:Movie) RETURN DISTINCT m.id, m.title, m.score ORDER BY m.score DESC LIMIT 15`,
        "Title",
        "Score",
        "movie"
    );
}

function bestScorePeoples() {
    bestNodes(
        `MATCH (p:People) RETURN DISTINCT p.id, p.name, p.score ORDER BY p.score DESC LIMIT 15`,
        "Name",
        "Score",
        "person"
    );
}

function bestDegreePeoples() {
    bestNodes(
        `MATCH (p:People) RETURN DISTINCT p.id, p.name, p.knowsDegree ORDER BY p.knowsDegree DESC LIMIT 15`,
        "Name",
        "Degree",
        "person"
    );
}

function bestDegreeGenres() {
    bestNodes(
        `MATCH (g:Genre) RETURN DISTINCT g.id, g.name, g.degree ORDER BY g.degree DESC LIMIT 15`,
        "Name",
        "Degree",
        "genre"
    );
}

function clearModalTableContent() {
    let tab = document.getElementById('modal-table');
    tab.innerHTML = '';
}

function defineTableHeader(hCol1, hCol2) {
    let tableHeadRef = document.getElementById('modal-table');
    let header = tableHeadRef.createTHead();
    let row = header.insertRow(0);
    let newHCell1 = row.insertCell(0);
    let newHCell2 = row.insertCell(1);
    let th1 = document.createElement("th");
    let th2 = document.createElement("th");
    th1.appendChild(document.createTextNode(hCol1));
    th2.appendChild(document.createTextNode(hCol2));
    newHCell1.appendChild(th1);
    newHCell2.appendChild(th2);
}

function defineTableBody(tb1, tb2) {
    let tableBodyRef = document.getElementById('modal-table');
    let body = tableBodyRef.createTBody();
    let row = body.insertRow();
    let newCell1  = row.insertCell(0);
    let newCell2  = row.insertCell(1);
    newCell1.appendChild(tb1);
    newCell2.appendChild(tb2);
}


/*
 * Algos
 */
function shortestPath() {
    const p1 = document.getElementById("SPP1").value;
    const p2 = document.getElementById("SPP2").value;
    const q = `MATCH (p1:People { name: '${p1}'}),(p2:People {name: '${p2}' }), p = shortestPath((p1)-[r:KNOWS *]-(p2)) RETURN p`;
    updateSearchBar(q);
    viz.renderWithCypher(q);
}

function communityPeople() {
    const p = document.getElementById("peopleCommunityInput").value;
    const q = `MATCH r=(p:People {name: '${p}'})--(pp:People {knowsCommunity: p.knowsCommunity} ) RETURN r`;
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
                .run(`MATCH (p:People) WHERE LOWER(p.name) CONTAINS LOWER("${query}") RETURN p.name`)
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
                .run(`MATCH (m:Movie) WHERE LOWER(m.title) CONTAINS LOWER("${query}") RETURN m.title`)
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
                .run(`MATCH (g:Genre) WHERE LOWER(g.name) CONTAINS LOWER("${query}") RETURN g.name`)
                .then(res => {
                    const records = Array.from(res.records);
                    const names = records.map(r => r._fields[0]).sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()));
                    callback(names);
                });
        }
    }
});
