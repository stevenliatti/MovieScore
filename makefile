run_neo4j:
	docker run --rm \
	--publish=7474:7474 --publish=7687:7687 \
	--env NEO4J_AUTH=neo4j/wem2020 \
	--env NEO4J_dbms_memory_pagecache_size=4G \
	--env NEO4J_dbms_memory_heap_max__size=4G \
	--user="$$(id -u):$$(id -g)" \
	neo4j:3.5

run_neo4j_with_vol:
	docker run --rm \
	--publish=7474:7474 --publish=7687:7687 \
	--volume="$$PWD"/neo4j-data:/data \
	--env NEO4J_AUTH=neo4j/wem2020 \
	--env NEO4J_dbms_memory_pagecache_size=4G \
	--env NEO4J_dbms_memory_heap_max__size=4G \
	--user="$$(id -u):$$(id -g)" \
	neo4j:3.5
