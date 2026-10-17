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
# JAVA_OPTS is injected at deploy time (e.g. -XX:MaxRAMPercentage=75.0 on Render's
# 512 MB free tier). The sh -c form lets the shell expand it; the exec form cannot.
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar /app/incidentvault-0.1.0.jar"]
