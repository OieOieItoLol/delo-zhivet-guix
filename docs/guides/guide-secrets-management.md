## Руководство 3. Секреты и переменные окружения

### Проблема

Переменные окружения делятся на две категории:

1. **Конфигурация** (не секретная): порты, пути, имена хостов, имена баз данных
2. **Секреты**: пароли, API-токены, приватные ключи

Для каждой категории — свой подход.

### Конфигурация: record fields

Значения конфигурации задаются прямо в Scheme-конфигурации сервиса:

```scheme
(service delo-zhivet-service-type
  (delo-zhivet-configuration
    (backend-port 9966)
    (bot-port 9967)
    (images-directory "/var/lib/delo-zhivet/images")
    (postgresql-database "tracker")))
```

Эти значения попадают в `/gnu/store` как часть Scheme-кода, но это безопасно — они не являются секретами.

### Секреты: env-файлы вне store

**Ключевой принцип:** секреты **никогда** не должны попадать в `/gnu/store`.

Почему:
- `/gnu/store` — мировое чтение по умолчанию
- Все объекты store сохраняются навсегда (пока есть ссылки)
- Бэкапы, логи, снимки диска могут содержать store
- При `guix gc` секреты остаются в мусоре

**Правильный подход:** хранить секреты в файлах вне store, а в Guix-конфигурации хранить только **пути** к этим файлам.

### Практическая схема для «Дело живёт»

**Структура на целевом сервере:**

```
/etc/delo-zhivet/
├── backend.env      # секреты backend
├── bot.env          # секреты bot
└── certs/
    ├── delo-zhivet.orgnarod.su.crt    # можно в store (публичный)
    └── delo-zhivet.orgnarod.su.key    # НЕ в store!
```

**Создание файлов администратором:**

```bash
# Создать директорию с правильными правами
sudo install -d -m 0750 -o root -g delo-zhivet /etc/delo-zhivet

# Создать пустые файлы для секретов
sudo install -m 0440 -o root -g delo-zhivet-backend /dev/null \
  /etc/delo-zhivet/backend.env

sudo install -m 0440 -o root -g delo-zhivet-bot /dev/null \
  /etc/delo-zhivet/bot.env

# Отредактировать секреты
sudo editor /etc/delo-zhivet/backend.env
sudo editor /etc/delo-zhivet/bot.env
```

**Содержимое `backend.env`:**

```dotenv
PG_PASSWORD=f_chc87Y#C&-Hc_Etu-32gr87N-nyfynhmisgg
BOT_BACKEND_SECRET=supersecret
BACKEND_BOT_SECRET=anothersecret
```

**Содержимое `bot.env`:**

```dotenv
PG_PASSWORD=f_chc87Y#C&-Hc_Etu-32gr87N-nyfynhmisgg
TELEGRAM_BOT_TOKEN=123456:ABC-DEF...
DADATA_CLIENT_TOKEN=xxx
APP_BACKEND_TOKEN=supersecret
```

**Guix-конфигурация** хранит только пути:

```scheme
(service delo-zhivet-service-type
  (delo-zhivet-configuration
    (backend-env-file "/etc/delo-zhivet/backend.env")
    (bot-env-file "/etc/delo-zhivet/bot.env")))
```

### Launcher, читающий env-файл

Каждый сервис запускается через Guile-программу (`program-file`), которая:

1. Читает env-файл
2. Парсит его безопасно (без shell `source`)
3. Проверяет имена переменных по allowlist
4. Устанавливает переменные через `setenv`
5. Вызывает `execl` для запуска Java

```scheme
(define (make-env-launcher env-file jar java allowlist)
  (program-file "launcher"
    #~(begin
        (use-modules (ice-9 rdelim) (ice-9 match))
        (let ((allowed (list #$@allowlist)))
          ;; Читаем env-файл
          (call-with-input-file #$env-file
            (lambda (port)
              (let loop ()
                (let ((line (read-line port)))
                  (unless (eof-object? line)
                    (let ((trimmed (string-trim-both line)))
                      (unless (or (string-null? trimmed)
                                 (string-prefix? "#" trimmed))
                        (match (string-split trimmed #\=)
                          ((name value)
                           (when (member name allowed)
                             (setenv name value)))))))
                  (unless (eof-object? line) (loop))))))
          ;; Запускаем Java
          (apply execl #$java
                 (list #$java "-jar" #$jar)))))
```

### Альтернатива: `secret-service-type`

В Guix существует встроенный `secret-service-type`, который копирует секреты в `/run/secrets/` (tmpfs, очищается при перезагрузке). Для нашего случая это избыточно — env-файлы проще и удобнее для Java-приложений, которые уже ожидают переменные окружения.
