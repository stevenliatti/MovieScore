var viz;

const URL_DB = "bolt://localhost:7687";
const USER = "neo4j";
const PWD = "wem2020";
const INITIAL_QUERY = "MATCH p=()-[r:BELONGS_TO]->() RETURN p LIMIT 50";

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
                "caption": "name"
            },
            "Genre": {
                "caption": "name"
            },
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





/*
 * User to side navbar
 */
$(document).ready(function () {
    $('#sidebarCollapse').on('click', function () {
        $('#sidebar').toggleClass('active');
        $(this).toggleClass('active');
    });
});
