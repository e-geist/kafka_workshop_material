# syntax=docker/dockerfile:1
FROM maven:3.9.15-amazoncorretto-25
WORKDIR /app
COPY . .
RUN --mount=type=cache,id=kafka-workshop-m2,target=/root/.m2,sharing=locked mvn -B package -DskipTests
CMD ["java", "-jar", "target/app.jar"]
