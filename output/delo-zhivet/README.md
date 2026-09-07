# Delo Zhivet Guix Packaging

Этот репозиторий содержит рецепты Guix пакетов и сервисов для проекта "Дело живёт".

## Ограничения и особенности сборки

Guix требует полной воспроизводимости и не допускает сетевых запросов во время сборки в build container.
В связи с тем, что полноценная сборка `backend` и `bot` (Java/Spring Boot/Gradle) требует упаковки большого числа Maven-зависимостей как отдельных Guix пакетов, а `frontend` (Node/Vite) требует упаковки NPM зависимостей, в рамках текущего решения применяются `*-bin` пакеты.

**Это честное временное deployment-решение. Пакеты устанавливают уже скомпилированные артефакты, собранные вне Guix.**
Ограничение: Полноценная Guix-сборка потребует упаковки всех зависимостей проекта, что пока невозможно в рамках текущей задачи.

## Как собрать релизные артефакты локально

Перед тем как эти пакеты можно будет установить, необходимо подготовить релизные артефакты и загрузить их в публичное хранилище. Скрипт не публикует артефакты, релиз должен быть доступен для скачивания без секретных токенов.

### Backend
1. Перейдите в директорию бэкенда:
   ```bash
   cd references/backend
   ```
2. Соберите jar:
   ```bash
   ./gradlew bootJar
   ```
3. Извлеките полученный артефакт (например, `build/libs/backend-1.0.0.jar`).

### Bot
1. Перейдите в директорию бота:
   ```bash
   cd references/bot
   ```
2. Соберите jar:
   ```bash
   ./gradlew bootJar
   ```
3. Извлеките полученный артефакт (например, `build/libs/bot-1.0.0.jar`).

### Frontend
1. Перейдите в директорию фронтенда:
   ```bash
   cd references/frontend
   ```
2. Установите зависимости и соберите статику:
   ```bash
   npm ci
   npm run build
   ```
3. Упакуйте директорию `dist/` в архив без лишнего верхнего каталога:
   ```bash
   tar -czvf frontend-1.0.0.tar.gz -C dist .
   ```

## Публикация артефактов

Опубликуйте артефакты в релизе (например, на GitHub). Загрузите:
- `backend-1.0.0.jar`
- `bot-1.0.0.jar`
- `frontend-1.0.0.tar.gz`

Затем вручную заполните URL в файле `output/delo-zhivet/packages.scm`. Найдите `uri` поля под комментариями вида `;; URL-MARKER: delo-zhivet-backend-bin` и впишите реальные ссылки.

## Использование скрипта хешей

Скрипт `fill-release-artifact-hashes.py` служит только для заполнения Guix-хешей в файле `packages.scm`. Он не публикует артефакты. Пользователь должен запускать его после создания релизных файлов локально, чтобы файл `packages.scm` содержал правильные хеши для скачивания.

Пример запуска скрипта:

```bash
output/scripts/fill-release-artifact-hashes.py \
  --packages output/delo-zhivet/packages.scm \
  delo-zhivet-backend-bin ./references/backend/build/libs/backend-1.0.0.jar \
  delo-zhivet-bot-bin ./references/bot/build/libs/bot-1.0.0.jar \
  delo-zhivet-frontend-bin ./references/frontend/frontend-1.0.0.tar.gz
```
Доступен флаг `--dry-run` для предварительного просмотра.
При изменении релизных артефактов скрипт нужно запускать заново.

## Секреты

Секреты (пароли к БД, токены) нельзя помещать в `/gnu/store`, в Guix конфигурацию или в этот репозиторий.
Они должны быть созданы вручную на целевой системе перед первым запуском.
Система при активации создаст файлы-заглушки, если их нет. Тем не менее, рекомендуется создать их заранее вручную.

Пример:

```bash
sudo mkdir -p /etc/delo-zhivet
sudo chown root:delo-zhivet /etc/delo-zhivet
sudo chmod 750 /etc/delo-zhivet
```

Заполните значения:

`/etc/delo-zhivet/backend.env`
```
PG_PASSWORD=supersecret
BOT_BACKEND_SECRET=secret1
BACKEND_BOT_SECRET=secret2
```

`/etc/delo-zhivet/bot.env`
```
PG_PASSWORD=supersecret
TELEGRAM_BOT_TOKEN=token
DADATA_CLIENT_TOKEN=token2
APP_BACKEND_TOKEN=secret1
```

Установите права на файлы:
```bash
sudo chown root:delo-zhivet-backend /etc/delo-zhivet/backend.env
sudo chmod 0440 /etc/delo-zhivet/backend.env

sudo chown root:delo-zhivet-bot /etc/delo-zhivet/bot.env
sudo chmod 0440 /etc/delo-zhivet/bot.env
```

## База данных

`postgresql-service-type` обычно не создает базу и пользователя автоматически (если не использовать специальные расширения).
Подготовьте базу данных:
- База: `tracker` (или ваше переопределенное имя)
- Пользователь: `site` (или ваше переопределенное имя)
- Пароль должен совпадать с `PG_PASSWORD` в файлах секретов.

## Применение системы

Пример системной конфигурации находится в `output/examples/delo-zhivet-system.scm`.
Примените конфигурацию на целевой машине:

```bash
guix system reconfigure output/examples/delo-zhivet-system.scm
```

## Проверка сервисов

Статус сервисов можно проверить командами:
```bash
herd status delo-zhivet-backend
herd status delo-zhivet-bot
herd status nginx
```

Логи сервисов находятся в:
- `/var/log/delo-zhivet/backend.log`
- `/var/log/delo-zhivet/bot.log`
