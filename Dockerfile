# Etapa 1: Compilación
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Aquí va tu comando de build
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Aquí copiamos el JAR que mencionaste en tu Start Command
COPY --from=build /app/target/api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
# Aquí va tu comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]