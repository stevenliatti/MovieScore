include .env
export

env.js: .env
	@echo "env = {" >> $@
	@echo "    URL_DB: '$(URL_DB)'," >> $@
	@echo "    USER: '$(USER)'," >> $@
	@echo "    PWD: '$(PWD)'" >> $@
	@echo "}" >> $@

show_env: .env
	@cat .env
