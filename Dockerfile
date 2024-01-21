# Stage 1
FROM gradle:7.6.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle fatJar --no-daemon

# Stage 2
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/accounting-chat-bot-1.0-all.jar app.jar
CMD ["java", "-jar", "app.jar"]
