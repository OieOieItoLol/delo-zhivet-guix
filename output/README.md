# Дело живёт — Guix Channel

Этот репозиторий содержит рецепты Guix для проекта «Дело живёт».

## Подключение канала

Для использования канала добавьте его в ваш `~/.config/guix/channels.scm`:

```scheme
(cons (channel
        (name 'delo-zhivet)
        (url "URL_РЕПОЗИТОРИЯ")
        (introduction
         (make-channel-introduction
          "ХЕШ_ПЕРВОГО_ПОДПИСАННОГО_КОММИТА"
          (openpgp-fingerprint "ОТПЕЧАТОК_КЛЮЧА"))))
      %default-channels)
```

## Инструкция по созданию GPG-ключа и подписи первого коммита

Канал защищен механизмом Channel authentication.

**Шаг 1. Создать ключ для канала:**

```bash
gpg --quick-gen-key "delo-zhivet-channel" ed25519 sign never
gpg --fingerprint "delo-zhivet-channel"
# Запомнить отпечаток: AAAA BBBB CCCC ...
```

**Шаг 2. Подписать первый коммит:**

```bash
git config user.signingkey AAAA...
git config commit.gpgsign true
git commit -S -m "Initial channel structure"
git log --show-signature  # запомнить хеш этого коммита
```

**Шаг 3. Добавить отпечаток** в `.guix-authorizations` в корне репозитория канала.

## Пример `operating-system` конфигурации

```scheme
(use-modules (gnu)
             (delo-zhivet services))

(operating-system
  ;; ... базовая конфигурация ОС ...
  (services
    (cons*
      (service delo-zhivet-service-type
               (delo-zhivet-configuration
                (backend-package delo-zhivet-backend)
                (bot-package delo-zhivet-bot)
                (frontend-package delo-zhivet-frontend)
                (java-package openjdk17)
                ;; Настройки БД, портов, SSL и т.д.
                ))
      %base-services)))
```

## Инструкция по созданию env-файлов

Секреты не должны попадать в `/gnu/store`. Создайте env-файлы для бэкенда и бота в `/etc/delo-zhivet`:

```bash
sudo install -d -m 0750 -o root -g delo-zhivet /etc/delo-zhivet
sudo install -m 0440 -o root -g delo-zhivet-backend /dev/null /etc/delo-zhivet/backend.env
sudo install -m 0440 -o root -g delo-zhivet-bot /dev/null /etc/delo-zhivet/bot.env
```

Затем заполните эти файлы необходимыми секретами (например, `PG_PASSWORD`).

## Три разных механизма подписей в Guix

Важно понимать разницу между тремя механизмами подписей в Guix:

| Механизм | Что защищает | Когда используется |
|---|---|---|
| **Upstream OpenPGP signatures** | Подлинность исходного кода проекта | `guix refresh -u` проверяет `.sig` файлы, ключи импортируются в `~/.config/guix/upstream/trustedkeys.kbx` |
| **Substitute server authorization** | Подлинность бинарных подложек (_substitutes_) | `guix archive --authorize` добавляет публичные ключи серверов сборки |
| **Channel authentication** | Подлинность самого Guix-канала | `.guix-authorizations` + `make-channel-introduction` (см. Подключение канала) |

**Обратите внимание:** НЕ используйте `guix archive --authorize` для upstream GPG keys. Это частая ошибка.

## Полезные команды

- **Обновить Guix и каналы:** `guix pull`
- **Собрать систему с новыми изменениями:** `guix system build config.scm` или `guix system reconfigure config.scm`
- **Проверить статус сервисов:** `herd status` (а также `herd status delo-zhivet-backend` и `herd status delo-zhivet-bot`)
