(define-module (delo-zhivet services)
  #:use-module (gnu)
  #:use-module (gnu services)
  #:use-module (gnu services shepherd)
  #:use-module (gnu services admin)
  #:use-module (gnu services web)
  #:use-module (gnu system shadow)
  #:use-module (gnu system file-systems)
  #:use-module (gnu packages admin)
  #:use-module (guix records)
  #:use-module (guix gexp)
  #:use-module (guix least-authority)
  #:use-module (guix packages)
  #:use-module (guix modules)
  #:use-module (ice-9 match)
  #:export (delo-zhivet-configuration
            delo-zhivet-configuration?
            delo-zhivet-service-type))

(define-record-type* <delo-zhivet-configuration>
  delo-zhivet-configuration make-delo-zhivet-configuration
  delo-zhivet-configuration?
  (backend-package             delo-zhivet-configuration-backend-package)
  (bot-package                 delo-zhivet-configuration-bot-package)
  (frontend-package            delo-zhivet-configuration-frontend-package)
  (java-package                delo-zhivet-configuration-java-package)
  (backend-env-file            delo-zhivet-configuration-backend-env-file
                               (default "/etc/delo-zhivet/backend.env"))
  (bot-env-file                delo-zhivet-configuration-bot-env-file
                               (default "/etc/delo-zhivet/bot.env"))
  (images-directory            delo-zhivet-configuration-images-directory
                               (default "/var/lib/delo-zhivet/images"))
  (postgresql-socket-directory delo-zhivet-configuration-postgresql-socket-directory
                               (default "/run/postgresql"))
  (postgresql-database         delo-zhivet-configuration-postgresql-database
                               (default "delo_zhivet"))
  (postgresql-user             delo-zhivet-configuration-postgresql-user
                               (default "delo_zhivet"))
  (backend-port                delo-zhivet-configuration-backend-port
                               (default 9966))
  (bot-port                    delo-zhivet-configuration-bot-port
                               (default 9967))
  (nginx?                      delo-zhivet-configuration-nginx?
                               (default #t))
  (server-name                 delo-zhivet-configuration-server-name
                               (default '("localhost")))
  (listen                      delo-zhivet-configuration-listen
                               (default '("80" "443 ssl")))
  (ssl-certificate             delo-zhivet-configuration-ssl-certificate
                               (default #f))
  (ssl-certificate-key         delo-zhivet-configuration-ssl-certificate-key
                               (default #f)))


(define (make-env-launcher env-file jar java allowlist)
  (program-file "launcher"
    #~(begin
        (use-modules (ice-9 rdelim) (ice-9 match) (srfi srfi-1) (srfi srfi-13) (srfi srfi-26))
        (let ((allowed (list #$@allowlist)))
          ;; Read env file
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
                               (when (member name allowed)
                                 (setenv name (string-join value "="))))
                              (_ #f))))
                        (unless (eof-object? line) (loop))))))))
            (lambda args #f))
          ;; Run Java
          (apply execl #$java
                 (list #$java "-jar" #$jar))))))

(define (make-delo-zhivet-wrapper launcher config)
  (let* ((ssl-key (delo-zhivet-configuration-ssl-certificate-key config))
         (ssl-dir (and ssl-key (dirname ssl-key))))
    (least-authority-wrapper
     launcher
     #:namespaces (delete 'net (delete 'user %namespaces))
     #:mappings (append
                 (list
                  (file-system-mapping
                   (source "/etc/delo-zhivet")
                   (target "/etc/delo-zhivet")
                   (writable? #f))
                  (file-system-mapping
                   (source (delo-zhivet-configuration-images-directory config))
                   (target (delo-zhivet-configuration-images-directory config))
                   (writable? #t))
                  (file-system-mapping
                   (source (delo-zhivet-configuration-postgresql-socket-directory config))
                   (target (delo-zhivet-configuration-postgresql-socket-directory config))
                   (writable? #f))
                  (file-system-mapping
                   (source "/var/log/delo-zhivet")
                   (target "/var/log/delo-zhivet")
                   (writable? #t)))
                 (if ssl-dir
                     (list (file-system-mapping
                            (source ssl-dir)
                            (target ssl-dir)
                            (writable? #f)))
                     '())))))

(define delo-zhivet-service-type
  (service-type
   (name 'delo-zhivet)
   (extensions
    (list
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
     (service-extension activation-service-type
                        (lambda (config)
                          (let ((images-dir (delo-zhivet-configuration-images-directory config)))
                            (with-imported-modules '((guix build utils))
                              #~(begin
                                  (use-modules (guix build utils))
                                  (mkdir-p "/etc/delo-zhivet")
                                  (mkdir-p "/var/log/delo-zhivet")
                                  (mkdir-p #$images-dir)
                                  (chown "/etc/delo-zhivet" (passwd:uid (getpw "root")) (group:gid (getgr "delo-zhivet")))
                                  (chmod "/etc/delo-zhivet" #o750)
                                  (chown "/var/log/delo-zhivet" (passwd:uid (getpw "root")) (group:gid (getgr "delo-zhivet")))
                                  (chmod "/var/log/delo-zhivet" #o770)
                                  (chown #$images-dir (passwd:uid (getpw "delo-zhivet-backend")) (group:gid (getgr "delo-zhivet")))
                                  (chmod #$images-dir #o750))))))
     (service-extension shepherd-root-service-type
                        (lambda (config)
                          (let* ((backend-pkg (delo-zhivet-configuration-backend-package config))
                                 (bot-pkg (delo-zhivet-configuration-bot-package config))
                                 (java-pkg (delo-zhivet-configuration-java-package config))
                                 (backend-env (delo-zhivet-configuration-backend-env-file config))
                                 (bot-env (delo-zhivet-configuration-bot-env-file config))
                                 (java-bin (file-append java-pkg "/bin/java"))
                                 (backend-jar (file-append backend-pkg "/lib/delo-zhivet-backend.jar"))
                                 (bot-jar (file-append bot-pkg "/lib/delo-zhivet-bot.jar"))
                                 (backend-launcher (make-env-launcher backend-env backend-jar java-bin '("PG_PASSWORD" "BOT_BACKEND_SECRET" "BACKEND_BOT_SECRET")))
                                 (bot-launcher (make-env-launcher bot-env bot-jar java-bin '("PG_PASSWORD" "TELEGRAM_BOT_TOKEN" "DADATA_CLIENT_TOKEN" "APP_BACKEND_TOKEN")))
                                 (backend-wrapper (make-delo-zhivet-wrapper backend-launcher config))
                                 (bot-wrapper (make-delo-zhivet-wrapper bot-launcher config)))
                            (list
                             (shepherd-service
                              (provision '(delo-zhivet-backend))
                              (requirement '(networking user-processes))
                              (start #~(make-forkexec-constructor
                                        (list #$backend-wrapper)
                                        #:user "delo-zhivet-backend"
                                        #:group "delo-zhivet"
                                        #:log-file "/var/log/delo-zhivet/backend.log"))
                              (stop #~(make-kill-destructor)))
                             (shepherd-service
                              (provision '(delo-zhivet-bot))
                              (requirement '(networking user-processes delo-zhivet-backend))
                              (start #~(make-forkexec-constructor
                                        (list #$bot-wrapper)
                                        #:user "delo-zhivet-bot"
                                        #:group "delo-zhivet"
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
                                      #~(string-append
                                         #$(delo-zhivet-configuration-frontend-package config)
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
                                        (body (list "try_files $uri $uri/ /index.html;")))))
                                     (ssl-certificate
                                      (delo-zhivet-configuration-ssl-certificate config))
                                     (ssl-certificate-key
                                      (delo-zhivet-configuration-ssl-certificate-key config))))
                              '())))))
   (default-value (delo-zhivet-configuration
                   (backend-package #f)
                   (bot-package #f)
                   (frontend-package #f)
                   (java-package #f)))
   (description "Runs the delo-zhivet backend, bot and configures nginx for the frontend.")))
