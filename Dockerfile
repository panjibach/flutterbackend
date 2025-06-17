# Stage 1: Build the application using Maven
FROM eclipse-temurin:21-jdk-jammy as builder

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# PASTIKAN BARIS INI ADA DI GITHUB ANDA
RUN chmod +x ./mvnw

RUN ./mvnw dependency:go-offline
COPY src ./src

# Jalankan build
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final, small image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Ambil file JAR dari stage sebelumnya
COPY --from=builder /app/target/flutterbackend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]