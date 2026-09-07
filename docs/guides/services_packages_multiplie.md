# Руководство по архитектуре составных сервисов и многовыходных пакетов в GNU Guix

*(Спецификация для LLM-агентов и системных инженеров)*

Данное руководство описывает идиоматичный способ упаковки монорепозиториев/микросервисов и построения составных (composite/meta) сервисов в GNU Guix.

---

## 1. Концепция: Многовыходные пакеты (Multiple Outputs)

Когда проект содержит несколько бинарных файлов (например, `api-server`, `worker`, `cli`), **нельзя** заставлять Guix собирать исходники заново под каждый сервис.

* **Решение**: Создается один пакет с несколькими выходами (`outputs`).
* **Результат**: Сборка (компиляция) выполняется **один раз**, но результат раскладывается по независимым путям в `/gnu/store`. Сервис, которому нужен только `worker`, притянет в рантайм исключительно бинарник `worker` без лишних зависимостей.

```scheme
(define-public my-project
  (package
    (name "my-project")
    (version "1.0.0")
    (source ...)
    (build-system gnu-build-system)
    ;; Объявляем выходы: "out" (по умолчанию), "api" и "worker"
    (outputs '("out" "api" "worker"))
    (arguments
     (#:phases
      (modify-phases %standard-phases
        (add-after 'install 'split-outputs
          (lambda* (#:key outputs #:allow-other-keys)
            (let ((out    (assoc-ref outputs "out"))
                  (api    (assoc-ref outputs "api"))
                  (worker (assoc-ref outputs "worker")))
              ;; Переносим исполняемые файлы по соответствующим выходам
              (mkdir-p (string-append api "/bin"))
              (mkdir-p (string-append worker "/bin"))
              (rename-file (string-append out "/bin/api-server")
                           (string-append api "/bin/api-server"))
              (rename-file (string-append out "/bin/worker")
                           (string-append worker "/bin/worker"))))))))
    (synopsis "Составной проект с API и Worker")
    (description "Основной пакет содержит CLI, а выходы api и worker — демоны.")
    (home-page "https://example.org")
    (license license:gpl3+)))

```

---

## 2. Ссылки на конкретные outputs в Scheme

Для ссылки на конкретный выход пакета используется процедура `(list package "output-name")`. В G-выражениях (`gexp`) для получения пути к бинарнику из конкретного выхода используется комбинация с `file-append`:

```scheme
;; Ссылка на выход "api" пакета my-project:
(file-append (list my-project "api") "/bin/api-server")

;; Ссылка на выход "worker" пакета my-project:
(file-append (list my-project "worker") "/bin/worker")

```

---

## 3. Архитектура составных сервисов (Meta-Services)

В GNU Guix **каждый запущенный процесс (демон) должен иметь свой собственный `shepherd-service**`. Нельзя запускать несколько демонов через один shell-скрипт или супервизор типа supervisord внутри одного Shepherd-процесса.

Чтобы не заставлять пользователя объявлять 10 отдельных сервисов в `operating-system`, используется **Зонтичный (Meta) сервис**.

### Шаблон реализации архитектуры «Зонтичного» сервиса

```scheme
(define-module (my-services project)
  #:use-module (guix records)
  #:use-module (guix gexp)
  #:use-module (guix least-authority)
  #:use-module (gnu services)
  #:use-module (gnu services shepherd)
  #:use-module (gnu system file-systems)
  #:use-module (gnu build linux-container)
  #:use-module (my-packages project) ; содержит my-project
  #:export (my-api-configuration
            my-worker-configuration
            my-project-configuration
            my-project-service-type))

;; -----------------------------------------------------------------------------
;; 1. Доменные конфигурации компонентов
;; -----------------------------------------------------------------------------

(define-record-type* <my-api-configuration>
  my-api-configuration make-my-api-configuration
  my-api-configuration?
  (port my-api-configuration-port (default 8080)))

(define-record-type* <my-worker-configuration>
  my-worker-configuration make-my-worker-configuration
  my-worker-configuration?
  (concurrency my-worker-configuration-concurrency (default 4)))

;; -----------------------------------------------------------------------------
;; 2. Единая зонтичная конфигурация
;; -----------------------------------------------------------------------------

(define-record-type* <my-project-configuration>
  my-project-configuration make-my-project-configuration
  my-project-configuration?
  ;; Пакет по умолчанию. Может быть переопределен пользователем
  (package my-project-package (default my-project))
  (api     my-project-api     (default (my-api-configuration)))
  (worker  my-project-worker  (default (my-worker-configuration))))

;; -----------------------------------------------------------------------------
;; 3. Изоляция каждого процесса (Least Authority Wrappers)
;; -----------------------------------------------------------------------------

(define (isolated-api-executable config)
  (let ((pkg (my-project-package config)))
    (least-authority-wrapper
     ;; Берем бинарник строго из выхода "api"
     (file-append (list pkg "api") "/bin/api-server")
     #:name "my-api-wrapper"
     #:mappings (list (file-system-mapping
                       (source "/var/log/my-project")
                       (target "/var/log/my-project")
                       (writable? #t)))
     #:namespaces (delq 'net %namespaces)))) ; Сохраняем доступ к сети

(define (isolated-worker-executable config)
  (let ((pkg (my-project-package config)))
    (least-authority-wrapper
     ;; Берем бинарник строго из выхода "worker"
     (file-append (list pkg "worker") "/bin/worker")
     #:name "my-worker-wrapper"
     #:mappings (list (file-system-mapping
                       (source "/var/log/my-project")
                       (target "/var/log/my-project")
                       (writable? #t)))
     #:namespaces %namespaces))) ; Полная изоляция (сеть не нужна)

;; -----------------------------------------------------------------------------
;; 4. Генерация отдельных Shepherd-сервисов
;; -----------------------------------------------------------------------------

(define (my-project-shepherd-services config)
  (let ((api-conf    (my-project-api config))
        (worker-conf (my-project-worker config)))
    (list
     ;; Сервис 1: API Server
     (shepherd-service
      (provision '(my-project-api))
      (requirement '(networking loopback syslogd))
      (start #~(make-forkexec-constructor
                (list #$(isolated-api-executable config)
                      "--port" #$(number->string (my-api-configuration-port api-conf)))
                #:user "my-project"
                #:group "my-project"
                #:log-file "/var/log/my-project/api.log"))
      (stop #~(make-kill-destructor)))

     ;; Сервис 2: Background Worker
     (shepherd-service
      (provision '(my-project-worker))
      ;; Указываем зависимость: Worker не запустится раньше, чем поднимется API!
      (requirement '(my-project-api))
      (start #~(make-forkexec-constructor
                (list #$(isolated-worker-executable config)
                      "--concurrency" #$(number->string (my-worker-configuration-concurrency worker-conf)))
                #:user "my-project"
                #:group "my-project"
                #:log-file "/var/log/my-project/worker.log"))
      (stop #~(make-kill-destructor))))))

;; -----------------------------------------------------------------------------
;; 5. Зонтичный Service Type
;; -----------------------------------------------------------------------------

(define my-project-service-type
  (service-type
   (name 'my-project)
   (extensions
    (list
     ;; Создание общего системного пользователя для всех поддемонов
     (service-extension account-service-type
                        (const (list (user-group (name "my-project") (system? #t))
                                     (user-account
                                      (name "my-project")
                                      (group "my-project")
                                      (system? #t)))))
     ;; Генерация всех shepherd-сервисов одной функцией
     (service-extension shepherd-root-service-type
                        my-project-shepherd-services)))
   (default-value (my-project-configuration))
   (description "Управляет полным стеком сервисов MyProject (API и Worker).")))

```

---

## 4. Чек-лист для мульти-сервисной архитектуры (Rules of Engagement)

При проектировании мульти-сервисов в Guix строго соблюдайте следующие правила:

1. **Разделение выходов (Outputs)**: Если монорепозиторий собирает несколько сервисных бинарников, обязательно определяйте `(outputs '("out" "api" "worker" ...))` и раскладывайте артефакты по соответствующим директориям в фазе установки.
2. **Использование `(list package "output")**`: При ссылке на не дефолтный выход пакета внутри G-выражения или `file-append` всегда передавайте список вида `(list pkg "output-name")`.
3. **Граф зависимостей Shepherd (`requirement`)**: Всегда явно выражайте межпроцессные зависимости в поле `requirement` (например, `(requirement '(my-project-api))`). Shepherd самостоятельно определит порядок запуска и завершения.
4. **Изоляция на уровне демона**: Создавайте отдельный `least-authority-wrapper` для *каждого* бинарника. У API и Worker должны быть разные права, правила проброса файловой системы и сетевые допуски.
5. **Атомарность системных учетных записей**: Сервисы одной экосистемы могут разделять единую группу или системного пользователя, создаваемого через общий `account-service-type` в зонтичном сервисе.

---
