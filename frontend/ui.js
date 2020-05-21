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
