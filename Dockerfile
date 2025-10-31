# --- 1. Etapa de Construcción (Build) ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# --- ¡LÍNEA AÑADIDA! ---
# Damos permisos de ejecución al script mvnw
RUN chmod +x ./mvnw

# Ahora este comando funcionará
RUN ./mvnw dependency:go-offline

# Copiamos el resto del código fuente y construimos el JAR
COPY src src
RUN ./mvnw clean install -DskipTests

# --- 2. Etapa Final (Run) ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos SOLO el JAR compilado desde la etapa 'builder'
# (Asegúrate que el nombre de tu JAR sea correcto, revísalo en tu pom.xml o carpeta 'target')
COPY --from=builder /app/target/Tienda-BuenaVision-0.0.1-SNAPSHOT.jar .

# Render nos dará un puerto en la variable $PORT.
EXPOSE 8080 

# Comando para arrancar la aplicación
CMD ["java", "-jar", "Tienda-BuenaVision-0.0.1-SNAPSHOT.jar"]