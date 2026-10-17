# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/incidentvault-0.1.0.jar /app/incidentvault-0.1.0.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/incidentvault-0.1.0.jar"]
