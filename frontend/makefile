include .env
export

env.js: .env
	@rm env.js
	@echo "env = {" >> $@
	@echo "    URL_DB: '$(URL_DB)'," >> $@
	@echo "    USER: '$(USER)'," >> $@
	@echo "    PWD: '$(PWD)'" >> $@
	@echo "}" >> $@
	@cat env.js

show_env: .env
	@cat .env
