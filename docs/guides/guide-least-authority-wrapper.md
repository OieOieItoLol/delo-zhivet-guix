## Руководство 4. Изоляция через wrapper

### Что такое `least-authority-wrapper`

Это Scheme-процедура из модуля `(guix least-authority)`, которая создаёт **исполняемый файл-обёртку** (_wrapper executable_). При запуске эта обёртка создаёт новые Linux namespaces и запускает целевую программу с минимальными привилегиями.

**Какие namespaces создаются:**

| Namespace | Что изолирует |
|---|---|
| `mount` | Файловую систему — программа видит только то, что явно разрешено |
| `user` | UID/GID — программа работает от непривилегированного пользователя |
| `pid` | Дерево процессов |
| `ipc` | Межпроцессное взаимодействие |
| `uts` | Hostname |
| `net` | Сетевые интерфейсы |

### Как wrapper работает с `/gnu/store`

**Ключевой момент:** wrapper **НЕ монтирует весь `/gnu/store`**. Вместо этого он:

1. Анализирует gexp-выражение запускаемой программы
2. Вычисляет её **closure** (_замыкание_ — все store-объекты, на которые программа прямо или косвенно ссылается)
3. Монтирует только эти объекты

Если программа использует `openjdk`, wrapper увидит это и смонтирует `/gnu/store/...-openjdk-...`. Если вы хотите, чтобы программа имела доступ к чему-то ещё, это должно быть **частью её замыкания**, а не отдельным маппингом.

### Правильный способ: явные references

Если launcher нужно что-то кроме closure (например, env-файл), используйте поле `#:mappings`:

```scheme
(least-authority-wrapper
 launcher-program
 #:user "delo-zhivet-backend"
 #:group "delo-zhivet"
 #:namespaces (delete 'net (delete 'user %namespaces))
 #:mappings (list
             (file-system-mapping
              (source "/etc/delo-zhivet")
              (target "/etc/delo-zhivet")
              (writable? #f))
             (file-system-mapping
              (source "/var/lib/delo-zhivet/images")
              (target "/var/lib/delo-zhivet/images")
              (writable? #t))
             (file-system-mapping
              (source "/run/postgresql")
              (target "/run/postgresql")
              (writable? #f))))
```

### Неправильные способы

❌ **Монтировать весь store:**

```scheme
;; НЕПРАВИЛЬНО!
(file-system-mapping
  (source "/gnu/store")
  (target "/gnu/store"))
```

Это полностью уничтожает смысл least authority.

❌ **Использовать `#:host-network?`:**

Такого параметра **не существует**. Сеть управляется через `#:namespaces`.

### Управление namespaces

Для «Дело живёт» backend и bot должны:

- Слушать на `127.0.0.1` (host loopback)
- Ходить в сеть (bot → Telegram API, Dadata API)
- Общаться между собой через `127.0.0.1:ports`

Поэтому из namespaces **исключаем** `'net`:

```scheme
#:namespaces (delete 'net (delete 'user %namespaces))
```

Почему исключаем `'user`:

Если `'user` включён, wrapper создаёт новый user namespace с маппингом UID, и параметр `#:user "delo-zhivet-backend"` применяется иначе (через setuid внутри namespace). Для простоты системных сервисов лучше запускать wrapper от root (через Shepherd), исключить `'user`, и позволить wrapper'у самому сделать `setuid`/`setgid`.

### Итоговая формула

```scheme
(define (make-delo-zhivet-wrapper launcher user group)
  (least-authority-wrapper
   launcher
   #:user user
   #:group group
   #:namespaces (delete 'net (delete 'user %namespaces))
   #:mappings (list
               (file-system-mapping
                (source "/etc/delo-zhivet")
                (target "/etc/delo-zhivet")
                (writable? #f))
               (file-system-mapping
                (source "/var/lib/delo-zhivet/images")
                (target "/var/lib/delo-zhivet/images")
                (writable? #t))
               (file-system-mapping
                (source "/run/postgresql")
                (target "/run/postgresql")
                (writable? #f))
               (file-system-mapping
                (source "/var/log/delo-zhivet")
                (target "/var/log/delo-zhivet")
                (writable? #t)))))
```
