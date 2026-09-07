## Руководство 7. PostgreSQL из Java через Unix socket

### Проблема

Стандартный JDBC URL:

```
jdbc:postgresql://host:5432/database
```

работает только по TCP. Для Unix domain sockets нужен специальный **socket factory** в classpath.

### Что нужно проверить в проекте

Ищем в `build.gradle.kts` backend и bot:

- есть ли зависимость `junixsocket` или аналог?
- используется ли `socketFactory` в connection string?

Если нет — Unix socket из Java **не будет работать** без изменений.

### Вариант A: добавить junixsocket (правильно)

1. Добавить в `build.gradle.kts`:

```kotlin
implementation("com.kohlschutter.junixsocket:junixsocket-core:2.8.0")
```

2. JDBC URL:

```properties
spring.datasource.url=jdbc:postgresql://localhost/delo_zhivet
spring.datasource.hikari.data-source-properties.socketFactory=org.newsclub.net.unix.socketfactory.UnixSocketFactory
spring.datasource.hikari.data-source-properties.socketFactoryArg=/run/postgresql
```

Это позволит подключаться к Unix socket без TCP.

### Вариант B: TCP loopback (прагматично)

Если не хочется менять зависимости:

```properties
spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/delo_zhivet
```

Внутри `least-authority-wrapper` с исключённым `'net` это работает безопасно:

- трафик идёт через host loopback
- не покидает ядро
- не проходит через сетевые интерфейсы
- недоступен извне

**Рекомендация для «Дело живёт»:** начать с варианта B (TCP loopback), так как это не требует изменений в исходниках. В будущем, если потребуется максимальная производительность или дополнительная изоляция, перейти на вариант A.
