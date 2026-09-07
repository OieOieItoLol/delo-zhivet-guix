## Руководство 8. SSL-сертификаты и приватные ключи

### Проблема

SSL-сертификат (`.crt`) — публичный объект, его можно хранить в `/gnu/store`.  
Приватный ключ (`.key`) — **секрет**, его нельзя помещать в store.

### Почему ключ нельзя в store

- `/gnu/store` доступен на чтение всем пользователям
- при `guix gc` объекты остаются, пока есть ссылки
- приватный ключ в store = приватный ключ навсегда в системе

### Правильный подход

**Сертификат** — можно через `local-file`:

```scheme
(ssl-certificate
 (local-file "/path/to/delo-zhivet.orgnarod.su.crt"))
```

Это скопирует файл в store, что безопасно для публичного сертификата.

**Приватный ключ** — только string path:

```scheme
(ssl-certificate-key "/etc/delo-zhivet/certs/delo-zhivet.orgnarod.su.key")
```

Ключ остаётся вне store. nginx (через wrapper) получает доступ к нему через `file-system-mapping`.

### Практическая инструкция

1. Создать директорию для ключа:

```bash
sudo install -d -m 0750 -o root -g delo-zhivet /etc/delo-zhivet/certs
sudo install -m 0440 -o root -g root /dev/null \
  /etc/delo-zhivet/certs/delo-zhivet.orgnarod.su.key
sudo editor /etc/delo-zhivet/certs/delo-zhivet.orgnarod.su.key
```

2. nginx wrapper должен иметь маппинг:

```scheme
(file-system-mapping
 (source "/etc/delo-zhivet/certs")
 (target "/etc/delo-zhivet/certs")
 (writable? #f))
```

3. В nginx.conf:

```nginx
ssl_certificate /etc/delo-zhivet/certs/delo-zhivet.orgnarod.su.crt;
ssl_certificate_key /etc/delo-zhivet/certs/delo-zhivet.orgnarod.su.key;
```

### Альтернатива: Let's Encrypt через ACME

Если сертификаты получаются автоматически через Let's Encrypt, Guix имеет `certbot-service-type`. Ключи живут в `/etc/letsencrypt/live/...`, nginx читает их оттуда. Для самоподписанных сертификатов (как у нас с `orgnarod.crt`) подходит ручной подход выше.

---

## Итоговые промпты для агента

### Промпт 0: `JULES.md` (файл целей)

```markdown
# Цели проекта

Ты работаешь над переводом проекта `delo-zhivet` с Docker на GNU Guix.

Тебе доступны:
- исходники в `references/`
- документация Guix в `docs/main_guix_docs/`
- руководства в `docs/guides/` — ОБЯЗАТЕЛЬНО читай их перед написанием кода

## Важные правила

1. Не помещай секреты в `/gnu/store`. Никаких `plain-file`, `local-file` для приватных ключей и паролей.
2. Секреты хранятся в env-файлах вне store. В Guix-конфигурации — только пути.
3. Backend и bot — отдельные Shepherd services, не bash-супервизор.
4. Не монтируй весь `/gnu/store` в `least-authority-wrapper`. Wrapper сам монтирует closure.
5. Не используй `host-network?` — такого параметра нет. Управляй сетью через `#:namespaces`.
6. Сборки Guix не должны скачивать зависимости из сети. Для Gradle/NPM используй `*-bin` пакеты из release artifacts.
7. Nginx интегрируется через `nginx-service-type`, не запускается вручную.
8. Unix socket для JDBC требует `junixsocket`. Если его нет в classpath — используй TCP loopback.
9. Channel authentication (`.guix-authorizations`) — это не upstream OpenPGP и не substitute keys.

## Результат

Все файлы помещай в `output/`. Структура:
- `output/delo-zhivet/packages.scm`
- `output/delo-zhivet/services.scm`
- `output/.guix-channel`
- `output/.guix-authorizations`
- `output/README.md`
- `output/examples/delo-zhivet-system.scm`
```
