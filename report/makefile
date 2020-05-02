report.pdf: README.md planning
	pandoc -s --highlight-style my_highlight.theme --number-sections README.md -o $@

docker_pandoc: ../README.md
	cp $^ .
	sed -i "s/.svg/.png/g" README.md
	sed -i "s/report/./g" README.md
	docker run -it --rm -v "$$PWD":/src stevenliatti/mypandoc bash

planning: planning.svg planning.png

planning.svg: mmdc planning.mmd
	./mmdc -i planning.mmd -o planning.svg -w 1000

planning.png: mmdc planning.mmd
	./mmdc -i planning.mmd -o planning.png -w 1000

mmdc: mermaid
	ln -fs mermaid/node_modules/.bin/mmdc

mermaid:
	mkdir -p $@ && cd $@ && npm install mermaid.cli
