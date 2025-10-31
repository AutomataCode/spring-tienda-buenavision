# --- 1. Etapa de Construcción (Build) ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean install -DskipTests

# --- 2. Etapa Final (Run) ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/Tienda-BuenaVision-0.0.1-SNAPSHOT.jar .

EXPOSE 8080
CMD ["java", "-jar", "Tienda-BuenaVision-0.0.1-SNAPSHOT.jar"]
