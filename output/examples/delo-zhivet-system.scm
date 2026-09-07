;; Example of an operating system configuration using delo-zhivet-service-type.
(use-modules (gnu)
             (gnu system)
             (gnu services)
             (gnu services web)
             (gnu services networking)
             (gnu services databases)
             (delo-zhivet services)
             (guix packages)
             (gnu packages java))
(use-service-modules desktop networking ssh web)
(use-package-modules bootloaders)

;; We use a dummy package just to represent the dependencies for demonstration
(define dummy-backend (package (name "delo-zhivet-backend") (version "0") (source #f) (build-system (@ (guix build-system trivial) trivial-build-system)) (synopsis "") (description "") (license #f) (home-page "")))
(define dummy-bot (package (name "delo-zhivet-bot") (version "0") (source #f) (build-system (@ (guix build-system trivial) trivial-build-system)) (synopsis "") (description "") (license #f) (home-page "")))
(define dummy-frontend (package (name "delo-zhivet-frontend") (version "0") (source #f) (build-system (@ (guix build-system trivial) trivial-build-system)) (synopsis "") (description "") (license #f) (home-page "")))

(operating-system
  (host-name "delo-zhivet-server")
  (timezone "Europe/Moscow")
  (locale "en_US.utf8")

  (bootloader (bootloader-configuration
               (bootloader grub-bootloader)
               (targets (list "/dev/sda"))))
  (file-systems (cons (file-system
                        (device (file-system-label "my-root"))
                        (mount-point "/")
                        (type "ext4"))
                      %base-file-systems))

  (services
   (append
    (list (service dhcp-client-service-type)
          (service openssh-service-type
                   (openssh-configuration
                    (permit-root-login #t)))

          ;; Example of configuring the database service, typically required for delo-zhivet
          (service postgresql-service-type
                   (postgresql-configuration
                    (postgresql postgresql-15)))

          ;; Our custom delo-zhivet service
          (service delo-zhivet-service-type
                   (delo-zhivet-configuration
                    (backend-package dummy-backend)
                    (bot-package dummy-bot)
                    (frontend-package dummy-frontend)
                    (java-package openjdk17)
                    (server-name '("delo-zhivet.orgnarod.su"))
                    ;; Usually we point to non-store paths for SSL certs/keys
                    ;; (ssl-certificate (local-file "/path/to/cert.crt"))
                    ;; (ssl-certificate-key "/etc/delo-zhivet/certs/key.key")
                    )))
    %base-services)))
