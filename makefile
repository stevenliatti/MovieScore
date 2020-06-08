include .env
export

show_env: .env show_collector_env show_parser_env
	@cat .env

show_collector_env:
	$(MAKE) -C collector show_env

show_parser_env:
	$(MAKE) -C parser show_env

show_frontend_env:
	$(MAKE) -C frontend show_env

neo4j: neo4j/plugins/neo4j-graph-data-science-1.1.1-standalone.jar
	docker run --rm \
	--publish=7474:7474 --publish=7687:7687 \
	--volume="$$PWD"/neo4j/conf:/conf \
	--volume="$$PWD"/neo4j/plugins:/plugins \
	--env-file=.env \
	--user="$$(id -u):$$(id -g)" \
	neo4j:3.5

neo4j_vol: neo4j/plugins/neo4j-graph-data-science-1.1.1-standalone.jar
	docker run --rm -d \
	--publish=7474:7474 --publish=7687:7687 \
	--volume="$$PWD"/neo4j/conf:/conf \
	--volume="$$PWD"/neo4j/plugins:/plugins \
	--volume="$$PWD"/neo4j/data:/data \
	--env-file=.env \
	--user="$$(id -u):$$(id -g)" \
	neo4j:3.5

neo4j/plugins/neo4j-graph-data-science-1.1.1-standalone.jar:
	mkdir -p neo4j/plugins neo4j/conf neo4j/data
	-wget -nc https://s3-eu-west-1.amazonaws.com/com.neo4j.graphalgorithms.dist/graph-data-science/neo4j-graph-data-science-1.1.1-standalone.zip --directory-prefix=neo4j/plugins
	unzip neo4j/plugins/neo4j-graph-data-science-1.1.1-standalone.zip -d neo4j/plugins
	rm neo4j/plugins/neo4j-graph-data-science-1.1.1-standalone.zip

.PHONY: neo4j
