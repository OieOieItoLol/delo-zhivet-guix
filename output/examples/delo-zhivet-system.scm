(use-modules (gnu)
             (gnu packages databases)
             (gnu packages java)
             (delo-zhivet packages)
             (delo-zhivet services)
             (gnu services databases)
             (gnu services networking)
             (gnu services ssh)
             (gnu services web))

(use-service-modules databases networking ssh web)

(operating-system
  (host-name "delo-zhivet-host")
  (timezone "Europe/Moscow")
  (locale "en_US.utf8")

  (bootloader (bootloader-configuration
               (bootloader grub-bootloader)
               (targets '("/dev/sda"))))

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
                    (port-number 2222)))

          (service postgresql-service-type
                   (postgresql-configuration
                    (postgresql postgresql-15)))

          (service nginx-service-type (nginx-configuration))

          (service delo-zhivet-service-type
                   (delo-zhivet-configuration
                    (backend-package delo-zhivet-backend-bin)
                    (bot-package delo-zhivet-bot-bin)
                    (frontend-package delo-zhivet-frontend-bin)
                    (java-package openjdk17)
                    (backend-env-file "/etc/delo-zhivet/backend.env")
                    (bot-env-file "/etc/delo-zhivet/bot.env")
                    (images-directory "/var/lib/delo-zhivet/images")
                    (postgresql-host "127.0.0.1")
                    (postgresql-port 5432)
                    (postgresql-database "tracker")
                    (postgresql-user "site")
                    (postgresql-use-socket? #f)
                    (local-postgresql? #t)
                    (backend-port 9966)
                    (bot-port 9967)
                    (nginx? #t)
                    (server-name '("delo-zhivet.example"))
                    (listen '("80"))
                    (ssl-certificate #f)
                    (ssl-certificate-key #f))))
    %base-services)))
