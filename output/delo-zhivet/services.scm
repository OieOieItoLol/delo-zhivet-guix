(define-module (delo-zhivet services)
  #:use-module (gnu services)
  #:use-module (gnu services admin)
  #:use-module (gnu services shepherd)
  #:use-module (gnu services web)
  #:use-module (gnu system shadow)
  #:use-module (gnu system file-systems)
  #:use-module (gnu build linux-container)
  #:use-module (gnu packages admin)
  #:use-module (guix gexp)
  #:use-module (guix least-authority)
  #:use-module (guix modules)
  #:use-module (guix records)
  #:use-module (ice-9 match)
  #:use-module (srfi srfi-1)
  #:export (delo-zhivet-configuration
            delo-zhivet-configuration?
            delo-zhivet-service-type))

(define-record-type* <delo-zhivet-configuration>
  delo-zhivet-configuration make-delo-zhivet-configuration
  delo-zhivet-configuration?
  (backend-package             delo-zhivet-configuration-backend-package
                               (default #f))
  (bot-package                 delo-zhivet-configuration-bot-package
                               (default #f))
  (frontend-package            delo-zhivet-configuration-frontend-package
                               (default #f))
  (java-package                delo-zhivet-configuration-java-package
                               (default #f))
  (backend-env-file            delo-zhivet-configuration-backend-env-file
                               (default "/etc/delo-zhivet/backend.env"))
  (bot-env-file                delo-zhivet-configuration-bot-env-file
                               (default "/etc/delo-zhivet/bot.env"))
  (images-directory            delo-zhivet-configuration-images-directory
                               (default "/var/lib/delo-zhivet/images"))
  (postgresql-host             delo-zhivet-configuration-postgresql-host
                               (default "127.0.0.1"))
  (postgresql-port             delo-zhivet-configuration-postgresql-port
                               (default 5432))
  (postgresql-database         delo-zhivet-configuration-postgresql-database
                               (default "tracker"))
  (postgresql-user             delo-zhivet-configuration-postgresql-user
                               (default "site"))
  (postgresql-use-socket?      delo-zhivet-configuration-postgresql-use-socket?
                               (default #f))
  (postgresql-socket-directory delo-zhivet-configuration-postgresql-socket-directory
                               (default "/run/postgresql"))
  (local-postgresql?           delo-zhivet-configuration-local-postgresql?
                               (default #f))
  (backend-port                delo-zhivet-configuration-backend-port
                               (default 9966))
  (bot-port                    delo-zhivet-configuration-bot-port
                               (default 9967))
  (nginx?                      delo-zhivet-configuration-nginx?
                               (default #t))
  (server-name                 delo-zhivet-configuration-server-name
                               (default '("localhost")))
  (listen                      delo-zhivet-configuration-listen
                               (default '("80")))
  (ssl-certificate             delo-zhivet-configuration-ssl-certificate
                               (default #f))
  (ssl-certificate-key         delo-zhivet-configuration-ssl-certificate-key
                               (default #f)))

(define (validate-configuration config)
  (let ((backend-pkg (delo-zhivet-configuration-backend-package config))
        (bot-pkg (delo-zhivet-configuration-bot-package config))
        (java-pkg (delo-zhivet-configuration-java-package config))
        (nginx? (delo-zhivet-configuration-nginx? config))
        (frontend-pkg (delo-zhivet-configuration-frontend-package config))
        (backend-env (delo-zhivet-configuration-backend-env-file config))
        (bot-env (delo-zhivet-configuration-bot-env-file config))
        (ssl-cert (delo-zhivet-configuration-ssl-certificate config))
        (ssl-key (delo-zhivet-configuration-ssl-certificate-key config))
        (listen-list (delo-zhivet-configuration-listen config))
        (pg-port (delo-zhivet-configuration-postgresql-port config))
        (b-port (delo-zhivet-configuration-backend-port config))
        (t-port (delo-zhivet-configuration-bot-port config))
        (pg-db (delo-zhivet-configuration-postgresql-database config))
        (pg-user (delo-zhivet-configuration-postgresql-user config))
        (pg-sock? (delo-zhivet-configuration-postgresql-use-socket? config)))

    (unless backend-pkg (error "backend-package must be specified"))
    (unless bot-pkg (error "bot-package must be specified"))
    (unless java-pkg (error "java-package must be specified"))
    (when nginx?
      (unless frontend-pkg (error "frontend-package must be specified when nginx? is #t")))

    (unless (absolute-file-name? backend-env)
      (error "backend-env-file must be an absolute path"))
    (unless (absolute-file-name? bot-env)
      (error "bot-env-file must be an absolute path"))

    (when (string-prefix? "/gnu/store/" backend-env)
      (error "backend-env-file must not be in /gnu/store"))
    (when (string-prefix? "/gnu/store/" bot-env)
      (error "bot-env-file must not be in /gnu/store"))

    (when ssl-key
      (unless (absolute-file-name? ssl-key)
        (error "ssl-certificate-key must be an absolute path"))
      (when (string-prefix? "/gnu/store/" ssl-key)
        (error "ssl-certificate-key must not be in /gnu/store")))

    (let ((has-ssl-listen? (any (lambda (l) (string-contains l "ssl")) listen-list)))
      (when has-ssl-listen?
        (unless (and ssl-cert ssl-key)
          (error "ssl-certificate and ssl-certificate-key must both be set when listen includes ssl")))
      (when (or (and ssl-cert (not ssl-key)) (and ssl-key (not ssl-cert)))
        (error "Both ssl-certificate and ssl-certificate-key must be provided together or neither")))

    (unless (and (integer? b-port) (<= 1 b-port 65535))
      (error "backend-port must be between 1 and 65535"))
    (unless (and (integer? t-port) (<= 1 t-port 65535))
      (error "bot-port must be between 1 and 65535"))
    (unless (and (integer? pg-port) (<= 1 pg-port 65535))
      (error "postgresql-port must be between 1 and 65535"))

    (when (or (not (string? pg-db)) (string-null? pg-db))
      (error "postgresql-database must not be empty"))
    (when (or (not (string? pg-user)) (string-null? pg-user))
      (error "postgresql-user must not be empty"))

    (when pg-sock?
      (error "junixsocket support is not confirmed, postgresql-use-socket? cannot be #t"))))

(define (make-env-launcher env-file jar java config allowlist non-secret-envs home tmpdir)
  (program-file "launcher"
    #~(begin
        (use-modules (ice-9 rdelim) (ice-9 match) (srfi srfi-1) (srfi srfi-13) (srfi srfi-26))
        (let ((allowed (list #$@allowlist)))

          ;; Export non-secret ENVs
          (for-each (lambda (pair)
                      (setenv (car pair) (cdr pair)))
                    (list #$@non-secret-envs))

          (setenv "HOME" #$home)
          (setenv "TMPDIR" #$tmpdir)

          ;; Read secret env file
          (unless (file-exists? #$env-file)
            (format (current-error-port) "Error: env file ~a does not exist.~%" #$env-file)
            (exit 1))

          (catch 'system-error
            (lambda ()
              (call-with-input-file #$env-file
                (lambda (port)
                  (let loop ()
                    (let ((line (read-line port)))
                      (unless (eof-object? line)
                        (let ((trimmed (string-trim-both line)))
                          (unless (or (string-null? trimmed)
                                     (string-prefix? "#" trimmed))
                            (match (string-split trimmed #\=)
                              ((name value ...)
                               (let ((name-trimmed (string-trim-both name)))
                                 (when (member name-trimmed allowed)
                                   (setenv name-trimmed (string-join value "=")))))
                              (_ #f))))
                        (unless (eof-object? line) (loop))))))))
            (lambda args
              (format (current-error-port) "Error reading env file.~%")
              (exit 1)))

          ;; Validate required secrets based on allowlist
          ;; (Assuming PG_PASSWORD is required for both, TELEGRAM_BOT_TOKEN for bot)
          (when (member "PG_PASSWORD" allowed)
            (unless (getenv "PG_PASSWORD")
              (format (current-error-port) "Error: Required variable PG_PASSWORD is missing.~%")
              (exit 1)))
          (when (member "TELEGRAM_BOT_TOKEN" allowed)
            (unless (getenv "TELEGRAM_BOT_TOKEN")
              (format (current-error-port) "Error: Required variable TELEGRAM_BOT_TOKEN is missing.~%")
              (exit 1)))

          ;; Run Java
          (apply execl #$java
                 (list #$java "-jar" #$jar))))))

(define (make-delo-zhivet-wrapper launcher config state-dir env-file)
  (least-authority-wrapper
   launcher
   #:namespaces (delete 'net (delete 'user %namespaces))
   #:mappings (list
               (file-system-mapping
                (source env-file)
                (target env-file)
                (writable? #f))
               (file-system-mapping
                (source state-dir)
                (target state-dir)
                (writable? #t))
               (file-system-mapping
                (source (string-append state-dir "/tmp"))
                (target (string-append state-dir "/tmp"))
                (writable? #t))
               (file-system-mapping
                (source (delo-zhivet-configuration-images-directory config))
                (target (delo-zhivet-configuration-images-directory config))
                (writable? #t))
               (file-system-mapping
                (source "/var/log/delo-zhivet")
                (target "/var/log/delo-zhivet")
                (writable? #t)))))

(define (get-jdbc-url config)
  (string-append
   "jdbc:postgresql://"
   (delo-zhivet-configuration-postgresql-host config)
   ":"
   (number->string (delo-zhivet-configuration-postgresql-port config))
   "/"
   (delo-zhivet-configuration-postgresql-database config)))

(define (delo-zhivet-activation config)
  (let ((images-dir (delo-zhivet-configuration-images-directory config))
        (backend-env (delo-zhivet-configuration-backend-env-file config))
        (bot-env (delo-zhivet-configuration-bot-env-file config)))
    (with-imported-modules '((guix build utils))
      #~(begin
          (use-modules (guix build utils))

          ;; Create directories
          (mkdir-p "/etc/delo-zhivet")
          (mkdir-p "/var/log/delo-zhivet")
          (mkdir-p "/var/lib/delo-zhivet/backend")
          (mkdir-p "/var/lib/delo-zhivet/backend/tmp")
          (mkdir-p "/var/lib/delo-zhivet/bot")
          (mkdir-p "/var/lib/delo-zhivet/bot/tmp")
          (mkdir-p #$images-dir)
          (mkdir-p (dirname #$backend-env))
          (mkdir-p (dirname #$bot-env))

          ;; Set ownership and permissions on directories
          (chown "/etc/delo-zhivet" (passwd:uid (getpw "root")) (group:gid (getgr "delo-zhivet")))
          (chmod "/etc/delo-zhivet" #o750)

          (chown "/var/log/delo-zhivet" (passwd:uid (getpw "root")) (group:gid (getgr "delo-zhivet")))
          (chmod "/var/log/delo-zhivet" #o770)

          (chown "/var/lib/delo-zhivet/backend" (passwd:uid (getpw "delo-zhivet-backend")) (group:gid (getgr "delo-zhivet-backend")))
          (chmod "/var/lib/delo-zhivet/backend" #o750)

          (chown "/var/lib/delo-zhivet/backend/tmp" (passwd:uid (getpw "delo-zhivet-backend")) (group:gid (getgr "delo-zhivet-backend")))
          (chmod "/var/lib/delo-zhivet/backend/tmp" #o750)

          (chown "/var/lib/delo-zhivet/bot" (passwd:uid (getpw "delo-zhivet-bot")) (group:gid (getgr "delo-zhivet-bot")))
          (chmod "/var/lib/delo-zhivet/bot" #o750)

          (chown "/var/lib/delo-zhivet/bot/tmp" (passwd:uid (getpw "delo-zhivet-bot")) (group:gid (getgr "delo-zhivet-bot")))
          (chmod "/var/lib/delo-zhivet/bot/tmp" #o750)

          (chown #$images-dir (passwd:uid (getpw "root")) (group:gid (getgr "delo-zhivet")))
          (chmod #$images-dir #o2770)

          ;; Create backend env file stub safely
          (unless (file-exists? #$backend-env)
            (call-with-output-file #$backend-env
              (lambda (port)
                (display "# Fill this file manually on the target host.\n" port)
                (display "# Do not put secrets into /gnu/store.\n#\n" port)
                (display "# PG_PASSWORD=...\n" port)
                (display "# BOT_BACKEND_SECRET=...\n" port)
                (display "# BACKEND_BOT_SECRET=...\n" port)))
            (chown #$backend-env (passwd:uid (getpw "root")) (group:gid (getgr "delo-zhivet-backend")))
            (chmod #$backend-env #o440))

          ;; Create bot env file stub safely
          (unless (file-exists? #$bot-env)
            (call-with-output-file #$bot-env
              (lambda (port)
                (display "# Fill this file manually on the target host.\n" port)
                (display "# Do not put secrets into /gnu/store.\n#\n" port)
                (display "# PG_PASSWORD=...\n" port)
                (display "# TELEGRAM_BOT_TOKEN=...\n" port)
                (display "# DADATA_CLIENT_TOKEN=...\n" port)
                (display "# APP_BACKEND_TOKEN=...\n" port)))
            (chown #$bot-env (passwd:uid (getpw "root")) (group:gid (getgr "delo-zhivet-bot")))
            (chmod #$bot-env #o440))))))

(define delo-zhivet-service-type
  (service-type
   (name 'delo-zhivet)
   (extensions
    (list
     (service-extension account-service-type
                        (lambda (config)
                          (list (user-group (name "delo-zhivet") (system? #t))
                                (user-group (name "delo-zhivet-backend") (system? #t))
                                (user-group (name "delo-zhivet-bot") (system? #t))
                                (user-account
                                 (name "delo-zhivet-backend")
                                 (group "delo-zhivet-backend")
                                 (supplementary-groups '("delo-zhivet"))
                                 (system? #t)
                                 (comment "Backend service")
                                 (home-directory "/var/empty")
                                 (shell (file-append shadow "/sbin/nologin")))
                                (user-account
                                 (name "delo-zhivet-bot")
                                 (group "delo-zhivet-bot")
                                 (supplementary-groups '("delo-zhivet"))
                                 (system? #t)
                                 (comment "Bot service")
                                 (home-directory "/var/empty")
                                 (shell (file-append shadow "/sbin/nologin"))))))
     (service-extension activation-service-type delo-zhivet-activation)
     (service-extension shepherd-root-service-type
                        (lambda (config)
                          (validate-configuration config)
                          (let* ((backend-pkg (delo-zhivet-configuration-backend-package config))
                                 (bot-pkg (delo-zhivet-configuration-bot-package config))
                                 (java-pkg (delo-zhivet-configuration-java-package config))
                                 (backend-env (delo-zhivet-configuration-backend-env-file config))
                                 (bot-env (delo-zhivet-configuration-bot-env-file config))
                                 (pg-user (delo-zhivet-configuration-postgresql-user config))
                                 (jdbc-url (get-jdbc-url config))
                                 (backend-port (delo-zhivet-configuration-backend-port config))
                                 (bot-port (delo-zhivet-configuration-bot-port config))
                                 (java-bin (file-append java-pkg "/bin/java"))

                                 ;; Use the correct paths: /share/java/<jar>
                                 (backend-jar (file-append backend-pkg "/share/java/delo-zhivet-backend.jar"))
                                 (bot-jar (file-append bot-pkg "/share/java/delo-zhivet-bot.jar"))

                                 (backend-reqs (if (delo-zhivet-configuration-local-postgresql? config)
                                                   '(networking user-processes postgresql)
                                                   '(networking user-processes)))
                                 (bot-reqs (if (delo-zhivet-configuration-local-postgresql? config)
                                               '(networking user-processes delo-zhivet-backend postgresql)
                                               '(networking user-processes delo-zhivet-backend)))

                                 (backend-launcher (make-env-launcher
                                                    backend-env
                                                    backend-jar
                                                    java-bin
                                                    config
                                                    '("PG_PASSWORD" "BOT_BACKEND_SECRET" "BACKEND_BOT_SECRET")
                                                    `(("SERVER_PORT" . ,(number->string backend-port))
                                                      ("SPRING_DATASOURCE_URL" . ,jdbc-url)
                                                      ("SPRING_DATASOURCE_USERNAME" . ,pg-user))
                                                    "/var/lib/delo-zhivet/backend"
                                                    "/var/lib/delo-zhivet/backend/tmp"))

                                 (bot-launcher (make-env-launcher
                                                bot-env
                                                bot-jar
                                                java-bin
                                                config
                                                '("PG_PASSWORD" "TELEGRAM_BOT_TOKEN" "DADATA_CLIENT_TOKEN" "APP_BACKEND_TOKEN")
                                                `(("SERVER_PORT" . ,(number->string bot-port))
                                                  ("SPRING_DATASOURCE_URL" . ,jdbc-url)
                                                  ("SPRING_DATASOURCE_USERNAME" . ,pg-user)
                                                  ("APP_BACKEND_URL" . ,(string-append "http://127.0.0.1:" (number->string backend-port))))
                                                "/var/lib/delo-zhivet/bot"
                                                "/var/lib/delo-zhivet/bot/tmp"))

                                 (backend-wrapper (make-delo-zhivet-wrapper backend-launcher config "/var/lib/delo-zhivet/backend" backend-env))
                                 (bot-wrapper (make-delo-zhivet-wrapper bot-launcher config "/var/lib/delo-zhivet/bot" bot-env)))

                            (list
                             (shepherd-service
                              (provision '(delo-zhivet-backend))
                              (requirement backend-reqs)
                              (start #~(make-forkexec-constructor
                                        (list #$backend-wrapper)
                                        #:user "delo-zhivet-backend"
                                        #:group "delo-zhivet-backend"
                                        #:log-file "/var/log/delo-zhivet/backend.log"))
                              (stop #~(make-kill-destructor)))
                             (shepherd-service
                              (provision '(delo-zhivet-bot))
                              (requirement bot-reqs)
                              (start #~(make-forkexec-constructor
                                        (list #$bot-wrapper)
                                        #:user "delo-zhivet-bot"
                                        #:group "delo-zhivet-bot"
                                        #:log-file "/var/log/delo-zhivet/bot.log"))
                              (stop #~(make-kill-destructor)))))))
     (service-extension nginx-service-type
                        (lambda (config)
                          (if (delo-zhivet-configuration-nginx? config)
                              (list (nginx-server-configuration
                                     (server-name
                                      (delo-zhivet-configuration-server-name config))
                                     (listen
                                      (delo-zhivet-configuration-listen config))
                                     (root
                                      (file-append (delo-zhivet-configuration-frontend-package config)
                                                   "/share/delo-zhivet-frontend"))
                                     (locations
                                      (list
                                       (nginx-location-configuration
                                        (uri "/api")
                                        (body (list
                                               (string-append
                                                "proxy_pass http://127.0.0.1:"
                                                (number->string
                                                 (delo-zhivet-configuration-backend-port config))
                                                ";"))))
                                       (nginx-location-configuration
                                        (uri "/images")
                                        (body (list
                                               (string-append
                                                "proxy_pass http://127.0.0.1:"
                                                (number->string
                                                 (delo-zhivet-configuration-backend-port config))
                                                "/images;"))))
                                       (nginx-location-configuration
                                        (uri "/")
                                        (body (list "try_files $uri /index.html;")))))
                                     (ssl-certificate
                                      (delo-zhivet-configuration-ssl-certificate config))
                                     (ssl-certificate-key
                                      (delo-zhivet-configuration-ssl-certificate-key config))))
                              '())))))
   (default-value (delo-zhivet-configuration))
   (description "Runs the delo-zhivet backend, bot and configures nginx for the frontend.")))
