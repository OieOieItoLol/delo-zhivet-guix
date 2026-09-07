## Руководство 1. Сборка проектов с сетевыми зависимостями

### Проблема

Guix по своей природе требует **воспроизводимости** (_reproducibility_). Сборка пакета происходит в изолированном контейнере (_build container_) без доступа к сети. Это значит, что привычные команды:

- `./gradlew build` (скачивает Gradle и Maven-зависимости)
- `npm ci` (скачивает пакеты из npm registry)
- `mvn package` (тянет артефакты из Maven Central)

**не работают** внутри Guix build environment. Они пытаются выполнить сетевые запросы, которые блокируются ядром.

### Решение: три уровня упаковки

#### Уровень 1. Полная Guix-native упаковка (идеал, но сложно)

Все зависимости проекта упаковываются как отдельные Guix-пакеты. Для Java — каждый Maven-артефакт становится `package` с `maven-build-system`. Для NPM — каждый npm-пакет становится `package` через `node-build-system`.

**Плюсы:** полная воспроизводимость, безопасность, возможность `guix refresh`.  
**Минусы:** для проекта с сотнями зависимостей это недели работы.

#### Уровень 2. Vendored dependencies с fixed-output derivation

Зависимости скачиваются **заранее** через `origin` с фиксированным хешем и подкладываются в сборку как inputs. Это называется **fixed-output derivation** (_детерминированная сборка с фиксированным результатом_) — Guix разрешает сетевой доступ только на этапе `url-fetch`/`git-fetch`, а сама сборка остаётся offline.

```scheme
(define maven-local-repo
  (origin
    (method url-fetch)
    (uri "https://example.com/delo-zhivet-deps.tar.gz")
    (sha256 (base32 "1abc..."))))

(package
  (name "delo-zhivet-backend")
  ...
  (arguments
   `(#:phases
     (modify-phases %standard-phases
       (add-before 'build 'setup-offline-repo
         (lambda* (#:key inputs #:allow-other-keys)
           (setenv "GRADLE_USER_HOME" (getcwd))
           (invoke "tar" "-xf" (assoc-ref inputs "maven-repo")))))))
  (native-inputs
   `(("maven-repo" ,maven-local-repo)
     ("gradle" ,gradle)
     ("jdk" ,openjdk)))))
```

**Плюсы:** настоящая сборка из исходников, offline.  
**Минусы:** нужно поддерживать архив зависимостей и пересчитывать его хеш при обновлениях.

#### Уровень 3. Пакет из release artifact’а (прагматичный путь)

Если upstream публикует готовые jar/zip/tarball с фиксированными хешами, мы упаковываем **уже собранный** артефакт. Это называется `*-bin` пакет.

```scheme
(define-public delo-zhivet-backend-bin
  (package
    (name "delo-zhivet-backend-bin")
    (version "1.0.0")
    (source
     (origin
       (method url-fetch)
       (uri (string-append "https://releases.example.com/backend-" version ".jar"))
       (sha256 (base32 "0xyz..."))))
    (build-system copy-build-system)
    (arguments
     `(#:install-plan '(("delo-zhivet-backend.jar" "share/java/"))))
    (inputs (list openjdk))))
```

**Плюсы:** быстро, просто, стабильно.  
**Минусы:** мы не контролируем процесс сборки, зависимость от upstream-репозитория бинарников.

### Решение для проекта «Дело живёт»

Для backend и bot (Spring Boot + Gradle): начинаем с **Уровня 3** — собираем jar локально через `./gradlew bootJar`, загружаем результат в публичное хранилище (например, в Git release assets или S3), и упаковываем как `*-bin`. Это честный и работающий подход. В README явно пишем:

> Пакет `delo-zhivet-backend` использует pre-built jar. Для полноценной upstream-упаковки необходимо упаковать ~150 Maven-зависимостей как отдельные Guix packages.

Для frontend (Vite + NPM): аналогично — `npm run build` локально, `dist/` выгружается как release artifact, упаковывается через `copy-build-system`.

### Практический шаблон

```scheme
(define-public delo-zhivet-backend
  (package
    (name "delo-zhivet-backend")
    (version "1.0.0")
    (source
     (origin
       (method git-fetch)
       (uri (git-reference
             (url "https://git.example.com/delo-zhivet.git")
             (commit version)))
       (file-name (git-file-name name version))
       (sha256 (base32 "ЗАМЕНИТЬ_ЧЕРЕЗ_guix_hash"))))
    (build-system trivial-build-system)
    (arguments
     `(#:modules ((guix build utils))
       #:builder
       (begin
         (use-modules (guix build utils))
         (let* ((out (assoc-ref %outputs "out"))
                (jar (string-append out "/share/java"))
                (src (assoc-ref %build-inputs "source")))
           (mkdir-p jar)
           ;; Здесь вызов gradlew невозможен — нет сети.
           ;; Решение: либо copy-build-system из release jar,
           ;; либо vendored deps (см. Уровень 2).
           (error "Build requires pre-built jar. See PACKAGING-BLOCKERS.md")))))
    (inputs (list openjdk))))
```
