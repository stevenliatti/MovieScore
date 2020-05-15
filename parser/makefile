run_neo4j_without_vol:
	docker run --rm \
	--publish=7474:7474 --publish=7687:7687 \
	--env NEO4J_AUTH=neo4j/wem2020 \
	neo4j:3.5

run_neo4j_with_vol:
	docker run --rm \
	--publish=7474:7474 --publish=7687:7687 \
	--volume=$HOME/neo4j/data:/data \
    --env NEO4J_AUTH=neo4j/wem2020 \
	neo4j:3.5
