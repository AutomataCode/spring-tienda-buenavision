# --- 1. Etapa de Construcción (Build) ---
# Usamos una imagen de Maven y Java 17 (definida en tu pom.xml) para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos los archivos de Maven y descargamos dependencias
# (Esto es una optimización de caché)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .
RUN ./mvnw dependency:go-offline

# Copiamos el resto del código fuente y construimos el JAR
COPY src src
RUN ./mvnw clean install -DskipTests

# --- 2. Etapa Final (Run) ---
# Usamos una imagen ligera de solo Java 17 para correr la app
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiamos SOLO el JAR compilado desde la etapa 'builder'
COPY --from=builder /app/target/Tienda-BuenaVision-0.0.1-SNAPSHOT.jar .

# Render nos dará un puerto en la variable $PORT.
# Nuestra app (en application.properties) ya está configurada para leer esto.
EXPOSE 8080 

# Comando para arrancar la aplicación
CMD ["java", "-jar", "Tienda-BuenaVision-0.0.1-SNAPSHOT.jar"]