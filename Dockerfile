# Use an official OpenJDK 21 runtime as the base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build file and source code
COPY pom.xml ./
COPY src ./src

# Copy the Maven wrapper (optional, ensures consistent Maven version)
COPY mvnw ./
COPY .mvn ./.mvn

# Grant execution permissions to mvnw and build the application
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# Run the JAR file
CMD ["java", "-jar", "target/activity-0.0.1-SNAPSHOT.jar"]
