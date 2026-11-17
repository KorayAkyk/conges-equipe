# Construction du jar, puis image d'execution minimale avec un utilisateur non root.
FROM maven:3.9.11-eclipse-temurin-21 AS construction
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:resolve
COPY src src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=construction /src/target/conges-equipe-1.0.0.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
