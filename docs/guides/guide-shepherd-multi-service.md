## Руководство 5. Запуск нескольких связанных процессов

### Проблема

Проект «Дело живёт» состоит из нескольких долгоживущих процессов:

- backend (Spring Boot, порт 9966)
- bot (Kotlin/Spring Boot, порт 9967)
- nginx (опционально)

Интуитивное решение — запустить их все из одного bash-скрипта через `wait`. **Это плохая архитектура** для Shepherd.

### Почему bash-супервизор плох

1. **Один упал — что делать с остальными?** Bash `wait` не умеет перезапускать отдельные процессы.
2. **Смешанные логи.** Вывод stdout/stderr всех процессов попадает в один файл.
3. **Нет health checks.** Shepherd не видит, какой именно процесс жив.
4. **Объединённые привилегии.** Оба процесса получают одинаковый mount namespace, filesystem authority, network access.
5. **Сигналы.** SIGTERM получает только bash-родитель; корректный shutdown дочерних процессов требует дополнительной логики (`trap`).
6. **Shell injection.** `source .env` в bash опасен — если в `.env` есть строка вроде `FOO=$(rm -rf /)`, она выполнится.

### Правильная архитектура: несколько Shepherd services

Shepherd — это и есть supervisor. Один `delo-zhivet-service-type` должен порождать **несколько отдельных Shepherd services**:

```
delo-zhivet-service-type (Guix service-type)
├── account-service-type extension (users/groups)
├── activation-service-type extension (directories)
├── shepherd-root-service-type extension:
│   ├── delo-zhivet-backend (Shepherd service)
│   └── delo-zhivet-bot (Shepherd service)
└── nginx-service-type extension (если nginx? #t)
```

### Определение нескольких Shepherd services

```scheme
(define delo-zhivet-service-type
  (service-type
   (name 'delo-zhivet)
   (extensions
    (list
     ;; Создание пользователей
     (service-extension account-service-type
                        (lambda (config)
                          (list (user-group (name "delo-zhivet") (system? #t))
                                (user-account
                                 (name "delo-zhivet-backend")
                                 (group "delo-zhivet")
                                 (system? #t)
                                 (comment "Backend service")
                                 (home-directory "/var/empty")
                                 (shell (file-append shadow "/sbin/nologin")))
                                (user-account
                                 (name "delo-zhivet-bot")
                                 (group "delo-zhivet")
                                 (system? #t)
                                 (comment "Bot service")
                                 (home-directory "/var/empty")
                                 (shell (file-append shadow "/sbin/nologin"))))))
     ;; Создание директорий
     (service-extension activation-service-type
                        (lambda (config)
                          (with-imported-modules '((guix build utils))
                            #~(begin
                                (use-modules (guix build utils))
                                (mkdir-p "/var/lib/delo-zhivet/images")
                                (chown "/var/lib/delo-zhivet" "delo-zhivet-backend" "delo-zhivet")
                                (chmod "/var/lib/delo-zhivet" #o750)))))
     ;; Shepherd services
     (service-extension shepherd-root-service-type
                        (lambda (config)
                          (list
                           (shepherd-service
                            (provision '(delo-zhivet-backend))
                            (requirement '(networking user-processes))
                            (start #~(make-forkexec-constructor
                                      (list #$(backend-wrapper config))
                                      #:log-file "/var/log/delo-zhivet/backend.log"))
                            (stop #~(make-kill-destructor)))
                           (shepherd-service
                            (provision '(delo-zhivet-bot))
                            (requirement '(networking user-processes delo-zhivet-backend))
                            (start #~(make-forkexec-constructor
                                      (list #$(bot-wrapper config))
                                      #:log-file "/var/log/delo-zhivet/bot.log"))
                            (stop #~(make-kill-destructor))))))))))
```

### Преимущества

- **Независимый перезапуск:** `herd restart delo-zhivet-bot` не трогает backend
- **Отдельные логи:** `/var/log/delo-zhivet/backend.log` и `/var/log/delo-zhivet/bot.log`
- **Отдельные привилегии:** у каждого свой wrapper, свои mappings
- **Корректные сигналы:** Shepherd посылает SIGTERM прямо процессу
- **Health check:** `herd status delo-zhivet-backend` показывает статус конкретного процесса

### Связь между процессами

Backend и bot общаются через HTTP на `127.0.0.1`. Поскольку мы исключили `'net` из namespaces, оба процесса видят host loopback и могут обращаться друг к другу:

- backend делает запрос на `http://127.0.0.1:9967/sendAuthCode` (bot endpoint)
- nginx (если есть) проксирует на `http://127.0.0.1:9966` и `http://127.0.0.1:9967`

Для PostgreSQL: оба процесса видят `/run/postgresql` через маппинг и подключаются через Unix socket (если поддерживает JDBC driver) или через `127.0.0.1:5432`.
