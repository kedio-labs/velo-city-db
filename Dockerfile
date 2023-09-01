FROM gradle:jdk11-alpine AS build

ENV CLEAR_CACHE true

WORKDIR /app
COPY build.gradle.kts gradle.properties settings.gradle.kts ./

COPY src ./src

RUN gradle fatJar
RUN ls /app
RUN ls /app/build/libs

FROM eclipse-temurin:11-jdk-alpine
WORKDIR /app
# There is actually only one JAR in the libs directory. The command below uses a wildcard so that it works with any version of the jar.
COPY --from=build /app/build/libs/*.jar velo-city-db-standalone.jar

ENTRYPOINT ["java", "-jar", "/app/velo-city-db-standalone.jar", "--data-directory-path", "/data"]