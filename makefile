run_neo4j: neo4j/plugins/neo4j-graph-data-science-1.1.1-standalone.jar
	docker run --rm \
	--publish=7474:7474 --publish=7687:7687 \
	--volume="$$PWD"/neo4j/conf:/conf \
	--volume="$$PWD"/neo4j/plugins:/plugins \
	--env NEO4J_AUTH=neo4j/wem2020 \
	--env NEO4J_dbms_memory_pagecache_size=4G \
	--env NEO4J_dbms_memory_heap_max__size=4G \
	--env NEO4J_dbms_security_procedures_unrestricted=gds.* \
	--user="$$(id -u):$$(id -g)" \
	neo4j:3.5

run_neo4j_with_vol: neo4j/plugins/neo4j-graph-data-science-1.1.1-standalone.jar
	docker run --rm \
	--publish=7474:7474 --publish=7687:7687 \
	--volume="$$PWD"/neo4j/conf:/conf \
	--volume="$$PWD"/neo4j/plugins:/plugins \
	--volume="$$PWD"/neo4j/data:/data \
	--env NEO4J_AUTH=neo4j/wem2020 \
	--env NEO4J_dbms_memory_pagecache_size=4G \
	--env NEO4J_dbms_memory_heap_max__size=4G \
	--env NEO4J_dbms_security_procedures_unrestricted=gds.* \
	--user="$$(id -u):$$(id -g)" \
	neo4j:3.5

neo4j/plugins/neo4j-graph-data-science-1.1.1-standalone.jar:
	mkdir -p neo4j/plugins neo4j/conf neo4j/data
	-wget -nc https://s3-eu-west-1.amazonaws.com/com.neo4j.graphalgorithms.dist/graph-data-science/neo4j-graph-data-science-1.1.1-standalone.zip --directory-prefix=neo4j/plugins
	unzip neo4j/plugins/neo4j-graph-data-science-1.1.1-standalone.zip -d neo4j/plugins
	rm neo4j/plugins/neo4j-graph-data-science-1.1.1-standalone.zip

