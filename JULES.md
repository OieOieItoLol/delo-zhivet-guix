# JULES.md

Ты работаешь над переводом проекта `delo-zhivet` с Docker-based deployment
на GNU Guix / Guix System.

В ходе выполнения для проверки кода обращайся к документации в `docs`:
- `guides` -- руководства проекта;
- `guix_manual_en` -- официальная документация guix;
- `guix-schemes_docs` -- документация и руководства по языку `Guilde Scheme`;
- `schemes` -- исходный код некоторых файлов из официального репозитория.

## Важные правила

1. Не помещай секреты в `/gnu/store`.
   Запрещено использовать `plain-file`, `mixed-text-file`, `local-file`,
   `computed-file` или Scheme record fields для значений секретов.

2. Секреты и deployment-specific env-переменные должны храниться во внешних
   файлах, создаваемых администратором на целевой машине, например:

   - `/etc/delo-zhivet/backend.env`
   - `/etc/delo-zhivet/bot.env`

   В Guix-конфигурации можно хранить только пути к этим файлам.

3. Не запускай несколько долгоживущих процессов из одного bash-супервизора.
   Backend и bot должны быть отдельными Shepherd services.

4. Если используешь `least-authority-wrapper`, не монтируй весь `/gnu/store`.
   Wrapper сам монтирует references/requisites запускаемого program.
   Если что-то из store не видно внутри контейнера, исправь references launcher’а,
   а не добавляй mapping `/gnu/store -> /gnu/store`.

5. Не используй несуществующие параметры вроде `host-network?` у
   `least-authority-wrapper`. Сетевые namespace управляются через `#:namespaces`.

6. Для host network / loopback нужно исключить `'net` из namespaces.
   Для применения `#:user` / `#:group` нужно исключить `'user` из namespaces.

7. Сборки Guix не должны скачивать зависимости из сети.
   `./gradlew`, Gradle Wrapper, `npm ci`, Maven/Gradle/NPM dependency downloads
   нельзя использовать как сетевые загрузчики во время Guix build.

8. Если полноценная Guix-native упаковка Java/NPM dependencies невозможна в рамках
   задачи, честно оформи это как blocker и создай временный `*-bin` пакет из
   заранее опубликованного release artifact’а с фиксированным hash. Не выдавай
   такой пакет за upstream-quality Guix package.

9. Nginx нужно интегрировать через существующий `nginx-service-type`, а не
   запускать отдельный nginx вручную, если нет очень веской причины.

10. PostgreSQL через Unix socket для Java возможен только если приложение /
    pgJDBC classpath поддерживают socket factory, например через junixsocket.
    Проверь это в исходниках. Не используй неподтверждённый JDBC URL.

## Цель

Создать Guix channel/modules для:

- пакетов backend, bot, frontend;
- Guix service-type `delo-zhivet-service-type`;
- optional nginx integration;
- README с инструкцией установки и управления секретами.

Все результаты помещай в `output/`.
