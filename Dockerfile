FROM adoptopenjdk/openjdk11:alpine
WORKDIR /app/
COPY lib/cplex ./cplex
COPY data ./data
COPY target/placementresolver-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-Djava.library.path=./cplex", "-jar", "app.jar"]