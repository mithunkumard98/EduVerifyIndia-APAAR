# Build the Spring Boot application with Java 17 and Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Run the packaged application on a small Java 17 runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/eduverify-india-1.1.0.jar app.jar
EXPOSE 10000
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar /app/app.jar"]
