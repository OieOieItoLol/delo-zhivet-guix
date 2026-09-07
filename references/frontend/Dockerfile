# ─── ЭТАП 1: Сборка фронтенда ─────────────────────────────────────
FROM node:22-alpine AS builder
WORKDIR /app

# Кэширование зависимостей
COPY package*.json ./
RUN npm ci

# Копируем исходный код и собираем
COPY . .
RUN npm run build   # → создаёт /app/dist

# ─── ЭТАП 2: Финальный образ с nginx ─────────────────────────────
FROM nginx:alpine

# Копируем собранную статику в стандартный путь nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# Копируем конфиг nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Сертификаты НЕ копируем — они будут примонтированы через volume
# Это позволяет обновлять сертификаты без пересборки образа
