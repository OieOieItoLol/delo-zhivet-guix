# ─── ЭТАП 1: Сборка ─────────────────────────────────────────────────
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

ARG PG_URL
ARG PG_USERNAME
ARG PG_PASSWORD

# Копируем wrapper явно
COPY gradlew ./
COPY gradle/wrapper/ ./gradle/wrapper/
COPY settings.gradle.kts build.gradle.kts gradle.properties ./

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew build -x test --no-daemon --info --stacktrace

# ─── ЭТАП 2: Runtime образ ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache graphicsmagick

WORKDIR /app
COPY --from=builder /app/build/libs/delo-zhivet-task-tracker-*.jar app.jar
RUN mkdir -p /opt/images

EXPOSE 9966
ENTRYPOINT ["java", "-jar", "app.jar"]
