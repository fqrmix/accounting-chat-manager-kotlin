# Stage 1
FROM gradle:7.5.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Stage 2
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/accounting-chat-bot-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]
