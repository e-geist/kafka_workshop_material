FROM maven:3.9.11-amazoncorretto-21
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests || mvn package -DskipTests
CMD ["java", "-cp", "target/producer-1.0-SNAPSHOT.jar", "com.example.producer.ProducerApp"]
