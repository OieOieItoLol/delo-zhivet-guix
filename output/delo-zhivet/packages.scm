(define-module (delo-zhivet packages)
  #:use-module (guix packages)
  #:use-module (guix download)
  #:use-module (guix build-system copy)
  #:use-module (guix build-system trivial)
  #:use-module (gnu packages java)
  #:use-module ((guix licenses) #:prefix license:)
  #:export (delo-zhivet-backend-bin
            delo-zhivet-bot-bin
            delo-zhivet-frontend-bin))

;; Это временный деплоймент-пакет.
;; Он устанавливает заранее собранный release artifact.
;; Полноценная Guix-сборка потребует упаковки всех зависимостей проекта,
;; что пока невозможно в рамках текущей задачи.
(define-public delo-zhivet-backend-bin
  (package
    (name "delo-zhivet-backend-bin")
    (version "1.0.0")
    (source
     (origin
       (method url-fetch)
       ;; URL-MARKER: delo-zhivet-backend-bin
       ;; TODO: заменить на реальный GitHub release asset URL.
       (uri "https://github.com/OWNER/REPO/releases/download/TAG/backend-1.0.0.jar")
       ;; HASH-MARKER: delo-zhivet-backend-bin
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
    (synopsis "Backend service for delo-zhivet (pre-built JAR)")
    (description "Backend service for delo-zhivet. Uses a pre-built JAR.")
    (home-page "https://github.com/OWNER/REPO")
    (license license:agpl3)))

;; Это временный деплоймент-пакет.
;; Он устанавливает заранее собранный release artifact.
;; Полноценная Guix-сборка потребует упаковки всех зависимостей проекта,
;; что пока невозможно в рамках текущей задачи.
(define-public delo-zhivet-bot-bin
  (package
    (name "delo-zhivet-bot-bin")
    (version "1.0.0")
    (source
     (origin
       (method url-fetch)
       ;; URL-MARKER: delo-zhivet-bot-bin
       ;; TODO: заменить на реальный GitHub release asset URL.
       (uri "https://github.com/OWNER/REPO/releases/download/TAG/bot-1.0.0.jar")
       ;; HASH-MARKER: delo-zhivet-bot-bin
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
    (synopsis "Telegram bot service for delo-zhivet (pre-built JAR)")
    (description "Telegram bot service for delo-zhivet. Uses a pre-built JAR.")
    (home-page "https://github.com/OWNER/REPO")
    (license license:agpl3)))

;; Это временный деплоймент-пакет.
;; Он устанавливает заранее собранный release artifact.
;; Полноценная Guix-сборка потребует упаковки всех зависимостей проекта,
;; что пока невозможно в рамках текущей задачи.
(define-public delo-zhivet-frontend-bin
  (package
    (name "delo-zhivet-frontend-bin")
    (version "1.0.0")
    (source
     (origin
       (method url-fetch)
       ;; URL-MARKER: delo-zhivet-frontend-bin
       ;; TODO: заменить на реальный GitHub release asset URL.
       (uri "https://github.com/OWNER/REPO/releases/download/TAG/frontend-1.0.0.tar.gz")
       ;; HASH-MARKER: delo-zhivet-frontend-bin
       (sha256 (base32 "0000000000000000000000000000000000000000000000000000"))))
    (build-system copy-build-system)
    (arguments
     `(#:install-plan '(("." "share/delo-zhivet-frontend/"))))
    (synopsis "Frontend static files for delo-zhivet (pre-built)")
    (description "Frontend static files for delo-zhivet. Pre-built via NPM.")
    (home-page "https://github.com/OWNER/REPO")
    (license license:agpl3)))
