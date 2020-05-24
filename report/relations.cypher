CREATE (p:People {name: 'People'})-[:PLAY_IN]->(m:Movie {name: 'Movie'})-[r:BELONGS_TO]->(g:Genre {name: 'Genre'})<-[:KNOWN_FOR_ACTING]-(p)-[:KNOWS]->(p)
CREATE (g)<-[:KNOWN_FOR_WORKING]-(p)-[:WORK_IN]->(m)-[:SIMILAR]->(m)-[:RECOMMENDATIONS]->(m)
