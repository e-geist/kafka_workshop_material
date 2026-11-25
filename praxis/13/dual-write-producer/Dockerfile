# syntax=docker/dockerfile:1
FROM maven:3.9.15-amazoncorretto-25
WORKDIR /app
COPY . .
RUN --mount=type=cache,id=kafka-workshop-m2,target=/root/.m2,sharing=locked mvn -B package -DskipTests
CMD ["java", "-cp", "target/dual-write-producer-1.0-SNAPSHOT.jar", "com.example.dualwrite.DualWriteProducerApp"]
