## Руководство 2. Три разных механизма подписей

### Проблема

В Guix существуют **три независимых** криптографических механизма, которые часто путают:

| Механизм | Что защищает | Когда используется |
|---|---|---|
| **Upstream OpenPGP signatures** | Подлинность исходного кода проекта | `guix refresh -u` проверяет `.sig` файлы |
| **Substitute server authorization** | Подлинность бинарных подложек (_substitutes_) | `guix archive --authorize` добавляет публичные ключи серверов сборки |
| **Channel authentication** | Подлинность самого Guix-канала | `.guix-authorizations` + `make-channel-introduction` |

Это **разные вещи**. Нельзя использовать один механизм вместо другого.

### Механизм 1. Upstream OpenPGP signatures

Когда upstream (авторы проекта) выпускают релиз, они могут подписать tarball или Git-тег своим GPG-ключом. Guix умеет проверять эти подписи через `guix refresh`.

**Как это работает:**

1. Guix скачивает tarball и связанный `.sig`/`.asc` файл
2. Проверяет подпись через GnuPG, используя keyring из `~/.config/guix/upstream/trustedkeys.kbx`
3. Если подпись валидна — вычисляет новый SHA-256 и обновляет рецепт

**Что нужно сделать:**

```bash
# Экспортировать ключ upstream в keyring Guix
gpg --export --armor upstream@example.com \
  >> ~/.config/guix/upstream/trustedkeys.kbx

# Обновить пакет
guix refresh -u delo-zhivet-backend
```

**Важно:** `guix archive --authorize` здесь **не используется**. Это частая ошибка.

### Механизм 2. Substitute server authorization

Guix позволяет скачивать готовые бинарные пакеты со специальных серверов (substitute servers), чтобы не собирать из исходников. Эти серверы подписывают свои архивы, и Guix должен знать их публичные ключи.

**Это то, для чего используется `guix archive --authorize`:**

```bash
# Добавить ключ substitute-сервера
sudo guix archive --authorize < /etc/guix/substitute-key.pub
```

Для нашего проекта substitute server пока не планируется, поэтому этот механизм нам не нужен.

### Механизм 3. Channel authentication

Каждый Guix-канал (Git-репозиторий с рецептами) должен быть аутентифицирован. При `guix pull` Guix проверяет **цепочку GPG-подписей каждого коммита** канала, начиная с момента введения (_introduction_).

**Как это работает:**

1. Первый подписанный коммит фиксируется в конфигурации канала:

```scheme
(channel
  (name 'delo-zhivet)
  (url "https://git.example.com/delo-zhivet-channel.git")
  (introduction
   (make-channel-introduction
    "ХЕШ_ПЕРВОГО_ПОДПИСАННОГО_КОММИТА"
    (openpgp-fingerprint "ОТПЕЧАТОК_КЛЮЧА"))))
```

2. В корне репозитория создаётся `.guix-authorizations`:

```scheme
(authorizations
  (version 0)
  (("ОТПЕЧАТОК_КЛЮЧА"
    (name "maintainer"))))
```

3. Каждый коммит подписывается: `git commit -S`

4. При `guix pull` Guix проверяет:
   - каждый коммит подписан ключом из `.guix-authorizations`
   - цепочка от текущего состояния до первого коммита не разорвана

**Это защищает от атаки:** злоумышленник не может добавить вредоносный рецепт в канал, даже если получит доступ к Git-серверу — у него нет GPG-ключа мейнтейнера.

### Практическая инструкция для «Дело живёт»

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

**Шаг 3. Создать `.guix-authorizations`** в корне репозитория канала.

**Шаг 4. Экспортировать ключ** в `etc/keyring.pgp`:

```bash
gpg --export --armor "delo-zhivet-channel" > etc/keyring.pgp
```

### Что НЕ надо делать

- ❌ Использовать `guix archive --authorize` для upstream-подписей
- ❌ Смешивать substitute keys и upstream keys
- ❌ Пытаться «автоматически проверять подписи Git-тегов» — это не встроено в `origin` по умолчанию
