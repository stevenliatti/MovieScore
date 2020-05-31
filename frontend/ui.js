var viz;

const URL_DB = "bolt://129.194.184.108:7687";
const USER = "neo4j";
const PWD = "wem2020";
var INITIAL_QUERY = "MATCH p=(:Genre)<-[:BELONGS_TO|:KNOWN_FOR_ACTING|:KNOWN_FOR_WORKING]-() RETURN p LIMIT 50";
const TMDB_URL = "https://www.themoviedb.org/"

const driver = neo4j.v1.driver(
    'bolt://129.194.184.108',
    neo4j.v1.auth.basic(USER, PWD)
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
        server_url: URL_DB,
        server_user: USER,
        server_password: PWD,
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
                size: sizeGenre
                // TODO : "size": "  | knownForWorkingDegree"
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
        relations += ':SIMILAR_JACCARD|';
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
 * Textual requests
 */
function best_score_movies() {
    session
        .run(`MATCH (m:Movie) RETURN m.id, m.title, m.score ORDER BY m.score DESC LIMIT 15`)
        .then(res => {
            const records = Array.from(res.records);
            const movies = records.map(r => {
                return {
                    title: r._fields[1],
                    score: r._fields[2],
                    linkTMDB: TMDB_URL + 'movie/' + r._fields[0]
                };
            });
            clearModalTableContent();
            defineTableHeader("Title", "Score", "TMDb link");
            movies.forEach(m => {
                let title = document.createTextNode(m.title);
                let score = document.createTextNode(Math.round(m.score * 100) / 100);
                let link =  document.createElement("a");
                link.appendChild(document.createTextNode("Show on TMDb"));
                link.href = m.linkTMDB;
                link.style.color = 'blue';
                defineTableBody(title, score, link);
            });
        });
}

function clearModalTableContent() {
    let tab = document.getElementById('modal-table');
    tab.innerHTML = '';
}

function defineTableHeader(hCol1, hCol2, hCol3) {
    let tableHeadRef = document.getElementById('modal-table');
    let header = tableHeadRef.createTHead();
    let row = header.insertRow(0);
    let newHCell1 = row.insertCell(0);
    let newHCell2 = row.insertCell(1);
    let newHCell3 = row.insertCell(2);
    let th1 = document.createElement("th");
    let th2 = document.createElement("th");
    let th3 = document.createElement("th");
    th1.appendChild(document.createTextNode(hCol1));
    th2.appendChild(document.createTextNode(hCol2));
    th3.appendChild(document.createTextNode(hCol3));
    newHCell1.appendChild(th1);
    newHCell2.appendChild(th2);
    newHCell3.appendChild(th3);
}

function defineTableBody(tb1, tb2, tb3) {
    let tableBodyRef = document.getElementById('modal-table');
    let body = tableBodyRef.createTBody();
    let row = body.insertRow();
    let newCell1  = row.insertCell(0);
    let newCell2  = row.insertCell(1);
    let newCell3  = row.insertCell(2);
    newCell1.appendChild(tb1);
    newCell2.appendChild(tb2);
    newCell3.appendChild(tb3);
}


/*
 * Algos
 */
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
