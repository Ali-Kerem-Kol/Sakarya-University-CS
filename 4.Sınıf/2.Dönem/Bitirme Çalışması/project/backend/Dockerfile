# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/*.jar app.jar

# Environment variable defaults
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Europe/Istanbul

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
