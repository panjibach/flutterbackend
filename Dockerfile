# Stage 1: Build the application using Maven
FROM eclipse-temurin:21-jdk-jammy as builder

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src

# Jalankan build dengan perintah yang sudah kita tahu berhasil
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final, small image to run the application
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Ambil file JAR yang sudah di-build dari stage sebelumnya
COPY --from=builder /app/target/flutterbackend-0.0.1-SNAPSHOT.jar app.jar

# Expose port yang digunakan oleh Spring Boot
EXPOSE 8080

# Perintah untuk menjalankan aplikasi
ENTRYPOINT ["java", "-jar", "app.jar"]