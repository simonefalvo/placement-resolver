ARTIFACT = placementresolver
JAR_VERSION = 0.0.1
JAR_NAME = ${ARTIFACT}-${JAR_VERSION}-SNAPSHOT.jar
IMAGE = smvfal/${ARTIFACT}
TAG = latest

publish: docker-build docker-push

docker-build:
	./mvnw clean package
	DOCKER_BUILDKIT=1 docker build -t ${IMAGE}:${TAG} .

docker-push:
	docker push ${IMAGE}:${TAG}

docker-run:
	docker run -p 8080:8080 --volume $(shell pwd)/data:/app/data/out ${IMAGE}:${TAG}

kube-deploy:
	kubectl apply -f kubernetes

build:
	./mvnw clean package

run:
	java -Djava.library.path=./lib/cplex -jar target/${JAR_NAME}

clean:
	./mvnw clean

cplex-install:
	./mvnw install:install-file -Dfile=lib/cplex-12.8.jar \
         -DgroupId=cplex -DartifactId=cplex \
         -Dversion=12.8 -Dpackaging=jar
