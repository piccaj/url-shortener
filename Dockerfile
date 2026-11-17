# Build stage: compile and package with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies separately from source so rebuilds after code-only
# changes don't re-download the internet
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# Run stage: slim JRE image with only the jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root user (most hosts expect/warn about this)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

# server.port is read from $PORT (see application.properties); defaults to 8080 locally
ENTRYPOINT ["java", "-jar", "app.jar"]
