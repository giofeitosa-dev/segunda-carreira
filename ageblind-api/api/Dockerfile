# --- Build (Maven + JDK 17, mesma versao da CI/Temurin) ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

# --- Runtime (JRE 17, usuario nao-root) ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/ageblind-api-0.0.1-SNAPSHOT.jar app.jar
USER 1001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
