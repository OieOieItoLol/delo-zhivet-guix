

## Руководство 6. `` — Интеграция с nginx-service-type

### Проблема

В Guix уже есть полноценный `nginx-service-type`. Не нужно запускать nginx вручную через свой wrapper.

### Правильный подход: extension

Наш `delo-zhivet-service-type` должен **расширять** `nginx-service-type` через `service-extension`, добавляя server block.

```scheme
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
                                   (uri "/")
                                   (body (list "try_files $uri $uri/ /index.html;")))))
                                (ssl-certificate
                                 (delo-zhivet-configuration-ssl-certificate config))
                                (ssl-certificate-key
                                 (delo-zhivet-configuration-ssl-certificate-key config))))
                         '())))
```

### Преимущества

- nginx управляется стандартным Guix-механизмом
- можно иметь несколько сервисов, расширяющих один nginx
- конфигурация объединяется автоматически
- reload через `herd reload nginx` работает корректно

### Альтернатива: отключить nginx из сервиса

Если `nginx?` = `#f`, extension возвращает пустой список `'()`, и наш сервис не добавляет ничего в nginx. В этом случае nginx настраивается отдельно (например, через Xray/OpenVPN-сценарий).
