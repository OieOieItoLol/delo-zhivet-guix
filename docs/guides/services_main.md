# Исчерпывающее руководство по архитектуре сервисов в GNU Guix

*(Спецификация для LLM-агентов и системных инженеров)*

Данное руководство описывает идиоматичный, безопасный и расширяемый подход к написанию сервисов в GNU Guix.

## 1. Фундаментальное разделение: Пакет vs Сервис

* **Пакет (`package`)**: Детерминированная сборка. Содержит исходники, патчи, инструкции `build-system`, результирующие бинарные файлы и статические ресурсы. **Никогда** не должен содержать системные конфиги, пути к сокетам, настройки пользователей или зависимости от конкретного окружения.
* **Сервис (`service-type`)**: Рантайм-оркестрация. Берет пакет и "оживляет" его: генерирует конфиги в `/gnu/store`, настраивает переменные окружения, создает пользователей, регистрирует процесс в `shepherd` и применяет политики безопасности (sandboxing).

## 2. Иерархия конфигураций (Композиция рекордов)

В Guile Scheme нет ООП-наследования. Для сложных сервисов (например, JVM + Приложение + Изоляция) используйте композицию через `define-record-type*`. Разделяйте конфигурацию на логические домены.

```scheme
(define-module (my-services java-app)
  #:use-module (guix records)
  #:use-module (guix gexp)
  #:use-module (guix least-authority)
  #:use-module (gnu services)
  #:use-module (gnu services shepherd)
  #:use-module (gnu system file-systems)
  #:use-module (gnu build linux-container)
  #:use-module (gnu packages java)
  #:export (jvm-configuration
            jvm-configuration?
            app-configuration
            app-configuration?
            java-app-service-configuration
            java-app-service-configuration?
            java-app-service-type))

;; Слой 1: Настройки виртуальной машины
(define-record-type* <jvm-configuration>
  jvm-configuration make-jvm-configuration
  jvm-configuration?
  (java        jvm-configuration-java        (default openjdk17))
  (max-heap    jvm-configuration-max-heap    (default "2G"))
  (extra-flags jvm-configuration-extra-flags (default '())))

;; Слой 2: Бизнес-логика и пути
(define-record-type* <app-configuration>
  app-configuration make-app-configuration
  app-configuration?
  (port        app-configuration-port        (default 8080))
  (db-socket   app-configuration-db-socket   (default "/var/run/postgresql")))

;; Слой 3: Корневая конфигурация сервиса
(define-record-type* <java-app-service-configuration>
  java-app-service-configuration make-java-app-service-configuration
  java-app-service-configuration?
  (package     java-app-service-package      (default my-app-package))
  (app         java-app-service-app-config   (default (app-configuration)))
  (jvm         java-app-service-jvm-config   (default (jvm-configuration)))
  (mappings    java-app-service-mappings     (default '())))

```

## 3. Генерация артефактов и G-выражения (Gexps)

Для создания конфигурационных файлов используйте `mixed-text-file` из `(guix gexp)`. Он позволяет безопасно интерполировать Scheme-переменные и ссылки на объекты Store.

```scheme
(define (app-config-file config)
  (mixed-text-file "my-app.conf"
    "server.port=" (number->string (app-configuration-port config)) "\n"
    "db.socket=" (app-configuration-db-socket config) "\n"))

```

## 4. Изоляция: Least Authority Wrapper

Изоляция должна быть **ортогональна** бизнес-логике. Мы не "вшиваем" контейнеризацию внутрь Java-скрипта. Вместо этого мы берем чистый бинарник и оборачиваем его в `least-authority-wrapper`. Этот wrapper создает исполняемый файл в Store, который при запуске настраивает Linux namespaces и seccomp.

```scheme
(define (isolated-java-executable config)
  (let ((jvm (java-app-service-jvm-config config))
        (app (java-app-service-app-config config))
        (extra-mappings (java-app-service-mappings config)))
    (least-authority-wrapper
     (file-append (jvm-configuration-java jvm) "/bin/java")
     #:name "my-app-java-wrapper"
     #:mappings (append
                 (list (file-system-mapping
                        (source (app-configuration-db-socket app))
                        (target (app-configuration-db-socket app))
                        (writable? #f))
                       (file-system-mapping
                        (source "/var/log/my-app")
                        (target "/var/log/my-app")
                        (writable? #t)))
                 extra-mappings)
     ;; Изолируем FS, PID, IPC, но оставляем сеть (исключаем 'net из %namespaces)
     #:namespaces (delq 'net %namespaces))))

```

## 5. Интеграция с Shepherd и Service Extensions

Сервис в Guix — это узел в графе зависимостей. Он должен автоматически создавать необходимых системных пользователей через `account-service-type` и регистрировать процесс через `shepherd-root-service-type`.

```scheme
(define (java-app-shepherd-service config)
  (let ((jvm (java-app-service-jvm-config config))
        (pkg (java-app-service-package config))
        (app (java-app-service-app-config config)))
    (shepherd-service
     (provision '(my-java-app))
     (requirement '(networking loopback syslogd))
     (start #~(make-forkexec-constructor
               ;; Передаем обернутый исполняемый файл как команду
               (list #$(isolated-java-executable config)
                     #$(string-append "-Xmx" (jvm-configuration-max-heap jvm))
                     #$@(jvm-configuration-extra-flags jvm)
                     "-jar" #$(file-append pkg "/share/java/my-app.jar")
                     "--config" #$(app-config-file app))
               #:user "my-app-user"
               #:group "my-app-group"
               #:log-file "/var/log/my-app/stdout.log"))
     (stop #~(make-kill-destructor)))))

(define java-app-service-type
  (service-type
   (name 'my-java-app)
   (extensions
    (list
     ;; Автоматическое создание пользователя и группы
     (service-extension account-service-type
                        (const (list (user-group (name "my-app-group") (system? #t))
                                     (user-account
                                      (name "my-app-user")
                                      (group "my-app-group")
                                      (system? #t)))))
     (service-extension shepherd-root-service-type
                        java-app-shepherd-service)))
   (default-value (java-app-service-configuration))
   (description "Изолированный Java-сервис.")))

```

## 6. Как делать сервисы расширяемыми для других?

Чтобы другие сервисы могли "подмешивать" свои настройки в ваш сервис (как это делает `nginx-service-type` или `mcron-service-type`), используйте поля `compose` и `extend` в определении `service-type`.

```scheme
;; Пример агрегирующего сервиса (например, сборщик метрик)
(define metrics-collector-service-type
  (service-type
   (name 'metrics-collector)
   (extensions (list (service-extension shepherd-root-service-type ...)))
   ;; Как объединять конфигурации от разных источников
   (compose append) 
   ;; Как применять объединенные конфигурации к базовой
   (extend (lambda (base-config extra-configs)
             (append base-config extra-configs)))
   (default-value '())))

```

Другие сервисы смогут расширять его: `(service-extension metrics-collector-service-type my-metrics-config)`.

## 7. Изоляция под конкретную машину (DevOps-слой)

Сервис должен оставаться generic (общим). Машинно-зависимая изоляция (например, проброс специфичных сертификатов или путей) достигается на этапе описания `operating-system` путем переопределения полей рекорда.

Вы можете создать функцию-конструктор, которая модифицирует базовый сервис под нужды ноды:

```scheme
(define (production-java-service config)
  (service java-app-service-type
           (java-app-service-configuration
            (inherit config)
            (jvm (jvm-configuration
                  (inherit (java-app-service-jvm-config config))
                  (max-heap "16G")))
            ;; Пробрасываем дополнительный путь через наш список маппингов
            (mappings (list (file-system-mapping
                             (source "/etc/ssl/certs")
                             (target "/etc/ssl/certs")
                             (writable? #f)))))))

```

## 8. Чек-лист для LLM-агента (Rules of Engagement)

При генерации Scheme-кода для Guix строго соблюдайте следующие правила:

1. **Никаких хардкодов Store**: Никогда не пишите пути вроде `/gnu/store/...-package`. Всегда используйте `(file-append package "/bin/exec")`.
2. **Детерминизм Gexps**: Внутри `#~` (gexp) используйте `#$` для внедрения одиночных объектов и `#$@` для внедрения списков (например, `#$@(list "-flag1" "-flag2")`).
3. **Отсутствие сайд-эффектов**: Никогда не используйте `system*`, `chmod`, `mkdir` на этапе описания сервиса. Все файлы и директории должны создаваться декларативно через `mixed-text-file`, `computed-file` или `file-append`.
4. **Безопасность по умолчанию**: Всегда стремитесь оборачивать сетевые и пользовательские сервисы в `least-authority-wrapper`. Оставляйте `writable? #f` для всех маппингов, кроме директорий для логов и баз данных.
5. **Импорты**: Всегда явно указывайте `#:use-module` для всех используемых функций. Guix не прощает пропущенных импортов.
6. **Современный API**: Забудьте про `make-forkexec-constructor/container`. Используйте `least-authority-wrapper` + `make-forkexec-constructor`.
