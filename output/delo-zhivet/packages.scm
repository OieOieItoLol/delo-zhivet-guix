(define-module (delo-zhivet packages)
  #:use-module (guix packages)
  #:use-module (guix download)
  #:use-module (guix build-system copy)
  #:use-module (guix build-system trivial)
  #:use-module (gnu packages java)
  #:use-module (guix licenses))

;; delo-zhivet-backend
;; Это временное deployment-решение (package из release artifact'а).
;; Для полноценной Guix-native сборки необходимо упаковать все Maven-зависимости (порядка 150)
;; как отдельные Guix packages, что невозможно сделать в рамках текущей задачи.
;; Сетевые запросы во время build в Guix запрещены, поэтому мы берем уже
;; собранный локально release jar.
(define-public delo-zhivet-backend
  (package
    (name "delo-zhivet-backend")
    (version "1.0.0")
    (source
     (origin
       (method url-fetch)
       ;; Пример URL, откуда скачивается release jar (надо обновить после релиза)
       (uri (string-append "https://releases.example.com/backend-" version ".jar"))
       (sha256 (base32 "0000000000000000000000000000000000000000000000000000"))))
    (build-system trivial-build-system)
    (arguments
     `(#:modules ((guix build utils))
       #:builder
       (begin
         (use-modules (guix build utils))
         (let* ((out (assoc-ref %outputs "out"))
                (jar-dir (string-append out "/share/java"))
                (src (assoc-ref %build-inputs "source")))
           (mkdir-p jar-dir)
           (copy-file src (string-append jar-dir "/delo-zhivet-backend.jar"))))))
    (inputs (list openjdk))
    (synopsis "Backend service for delo-zhivet")
    (description "Backend service for delo-zhivet. Uses a pre-built JAR.")
    (home-page "https://example.com")
    (license agpl3)))

;; delo-zhivet-bot
;; Это временное deployment-решение (package из release artifact'а).
;; Как и с backend, полноценная сборка требует Guix-native упаковки
;; всех зависимостей. Для обхода ограничений на сетевой доступ во время
;; сборки, используется заранее собранный jar.
(define-public delo-zhivet-bot
  (package
    (name "delo-zhivet-bot")
    (version "1.0.0")
    (source
     (origin
       (method url-fetch)
       ;; Пример URL, откуда скачивается release jar
       (uri (string-append "https://releases.example.com/bot-" version ".jar"))
       (sha256 (base32 "0000000000000000000000000000000000000000000000000000"))))
    (build-system trivial-build-system)
    (arguments
     `(#:modules ((guix build utils))
       #:builder
       (begin
         (use-modules (guix build utils))
         (let* ((out (assoc-ref %outputs "out"))
                (jar-dir (string-append out "/share/java"))
                (src (assoc-ref %build-inputs "source")))
           (mkdir-p jar-dir)
           (copy-file src (string-append jar-dir "/delo-zhivet-bot.jar"))))))
    (inputs (list openjdk))
    (synopsis "Telegram bot service for delo-zhivet")
    (description "Telegram bot service for delo-zhivet. Uses a pre-built JAR.")
    (home-page "https://example.com")
    (license agpl3)))

;; delo-zhivet-frontend
;; Это временное deployment-решение.
;; Для сборки frontend требуется NPM и сеть для загрузки зависимостей,
;; что запрещено в Guix build environment.
;; Данный пакет берет заранее собранный (через npm run build)
;; набор статики из архива dist и помещает в share/delo-zhivet-frontend/.
(define-public delo-zhivet-frontend
  (package
    (name "delo-zhivet-frontend")
    (version "1.0.0")
    (source
     (origin
       (method url-fetch)
       ;; Пример URL для загрузки собранной статики
       (uri (string-append "https://releases.example.com/frontend-" version ".tar.gz"))
       (sha256 (base32 "0000000000000000000000000000000000000000000000000000"))))
    (build-system copy-build-system)
    (arguments
     `(#:install-plan '(("." "share/delo-zhivet-frontend/"))))
    (synopsis "Frontend static files for delo-zhivet")
    (description "Frontend static files for delo-zhivet. Pre-built via NPM.")
    (home-page "https://example.com")
    (license agpl3)))
