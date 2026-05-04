# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
# Copy the pom.xml and download dependencies (caches them if pom hasn't changed)
COPY backend/pom.xml .
RUN mvn dependency:go-offline || true

# Copy backend source code
COPY backend/src ./src

# Copy frontend source code into Spring Boot static directory
COPY frontend ./src/main/resources/static

RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Railway injects the PORT environment variable
ENV PORT=8080
EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
