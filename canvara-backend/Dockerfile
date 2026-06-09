# ── Stage 1: Build ────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Run ──────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd -r canvara && useradd -r -g canvara canvara

COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/uploads && chown -R canvara:canvara /app

USER canvara

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]