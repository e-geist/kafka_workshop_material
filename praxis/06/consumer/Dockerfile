FROM maven:3.9.11-amazoncorretto-21
WORKDIR /app
COPY . .
RUN ./mvn package -DskipTests || mvn package -DskipTests
CMD ["java", "-cp", "target/consumer-1.0-SNAPSHOT.jar", "com.example.consumer.ConsumerApp"]
