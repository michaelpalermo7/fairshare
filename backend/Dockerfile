# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd -u 10001 -r -s /sbin/nologin appuser
USER appuser

ENV JAVA_OPTS="-Xms256m -Xmx512m"

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
