VELOCITYDB_VERSION=0.2.0
DATA_DIRECTORY_ABSOLUTE_PATH=${CURDIR}/data

.PHONY: build-fatjar
build-fatjar:
	@echo "== build fatjar"
	./gradlew fatJar

.PHONY: test-unit
test-unit:
	@echo "== test-unit"
	./gradlew test

.PHONY: test-integration
test-integration:
	@echo "== test-integration"
	./gradlew integrationTest

.PHONY: test
test:
	@echo "== test"
	./gradlew test integrationTest

.PHONY: docker-build
docker-build:
	@echo "== docker-build"
	docker build -t velocitydb .

.PHONY: docker-run
docker-run:
	@echo "== docker-run"
	mkdir -p $(DATA_DIRECTORY_ABSOLUTE_PATH)
	docker run --user $(shell id -u):$(shell id -g) -v $(DATA_DIRECTORY_ABSOLUTE_PATH):/data:rw velocitydb

.PHONY: run
run:
	@echo "== run"
	mkdir -p $(DATA_DIRECTORY_ABSOLUTE_PATH)
	java -jar ./build/libs/velo-city-db-$(VELOCITYDB_VERSION)-standalone.jar --data-directory-path $(DATA_DIRECTORY_ABSOLUTE_PATH)