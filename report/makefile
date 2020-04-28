planning: mmdc planning.mmd
	./mmdc -i planning.mmd -o planning.svg -w 1000

mmdc: mermaid
	ln -s mermaid/node_modules/.bin/mmdc

mermaid:
	mkdir -p $@ && cd $@ && npm install mermaid.cli
