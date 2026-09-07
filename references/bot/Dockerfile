# ─── ЭТАП 1: Сборка ─────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# 1. Копируем только файлы сборки для кэширования зависимостей
COPY gradlew ./
COPY gradle/wrapper/ ./gradle/wrapper/
COPY settings.gradle.kts build.gradle.kts gradle.properties ./

# 2. Делаем gradlew исполняемым и качаем зависимости
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 3. Копируем исходный код
COPY src ./src

# 4. Собираем проект
# -x test: пропускаем тесты (не нужна БД на этапе сборки)
# --no-daemon: экономия памяти в контейнере
RUN ./gradlew build -x test -x ktlintMainSourceSetCheck -x ktlintTestSourceSetCheck --no-daemon

# В stage builder, после RUN ./gradlew build ...
RUN ls -la /build/build/libs/

# ─── ЭТАП 2: Runtime образ ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем артефакт. Используем wildcard для удобства, как в бэкенде.
# Если сборка создает только один jar, это безопасно.
COPY --from=builder /build/build/libs/delo-zhivet-bot-*-SNAPSHOT.jar app.jar

# Переменные окружения (значения подставятся из docker-compose)
ENV TELEGRAM_BOT_TOKEN=""
ENV DADATA_CLIENT_TOKEN=""

ENTRYPOINT ["java", "-jar", "app.jar"]
