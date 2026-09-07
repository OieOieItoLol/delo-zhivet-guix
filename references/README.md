

## Предварительно

### База данных
```bash
-- Выполните от имени postgres
sudo -u postgres psql
```

Внутри psql:
```sql
CREATE USER site WITH PASSWORD 'f_chc87Y#C&-Hc_Etu-32gr87N-nyfynhmisgg';
CREATE DATABASE tracker;
GRANT ALL PRIVILEGES ON DATABASE tracker TO site;

-- Подключитесь к базе и дайте права на схему
\c tracker
GRANT ALL PRIVILEGES ON SCHEMA public TO site;

```

Ставим расширение:
```bash
# Установите пакет PostGIS для PostgreSQL 18
sudo apt-get update
sudo apt-get install postgresql-18-postgis-3

# Перезагрузите PostgreSQL (на всякий случай)
sudo systemctl reload postgresql

# Теперь создайте расширение

sudo -u postgres psql -d tracker -c "CREATE EXTENSION IF NOT EXISTS postgis;"

```

Добавляем:
```
# /etc/postgresql/18/main/postgresql.conf
listen_addresses = 'localhost,172.17.0.1'
```
Для того чтобы к postgresql можно было обращаться только с localhost и 172.17.0.1 (стандартный шлюз докера).

Также надо добавить:
```
# /etc/postgresql/18/main/pg_hba.conf

# Локальные подключения (Unix socket)
local   all     all                             peer

# Локальные TCP подключения
host    all     all     127.0.0.1/32            scram-sha-256
host    all     all     ::1/128                 scram-sha-256

# Docker контейнеры (только подсеть Docker)
host    all     all     172.17.0.0/16           scram-sha-256
```



### Корневой сертификат
Корневой сертификат был сгенерирован:
```bash
# Генерация приватного ключа корневого центра
openssl genrsa -out ./orgnarod.key 4096

# Создание самоподписанного корневого сертификата
openssl req -x509 -new -nodes -key orgnarod.key \
  -sha256 -days 3650 \
  -out ./orgnarod.crt \
  -subj "/CN=Narod CA/O=OrgNarod/C=RU" \
  -addext "basicConstraints=critical,CA:TRUE" \
  -addext "keyUsage=critical,keyCertSign,cRLSign"
```

Отпечатки сертификата:
```
sha256 Fingerprint=2F:92:70:5F:D2:6C:03:CE:03:91:43:AD:E1:2B:FB:94:0B:00:A4:6F:A1:28:86:E6:3A:BD:74:97:37:7D:3B:EE
sha1 Fingerprint=5D:54:B9:8B:22:F2:4C:EB:B2:0E:E7:4F:61:9E:CE:9E:B7:6C:99:94
md5 Fingerprint=BE:21:C9:B3:2D:9F:93:64:77:65:F7:C4:97:4B:14:1D
```

### Дочерние

#### Дело живёт

```bash
openssl genrsa -out delo-zhivet.orgnarod.su.key 4096

openssl req -new -key delo-zhivet.orgnarod.su.key -out delo-zhivet.orgnarod.su.csr -config delo-zhivet.orgnarod.su.cfg
```

Подпись корневым:
```bash
openssl x509 -req -in delo-zhivet.orgnarod.su.csr \
    -CA orgnarod.crt -CAkey orgnarod.key \
    -CAcreateserial \
    -out delo-zhivet.orgnarod.su.crt \
    -days 365 \
    -sha256 \
    -extfile delo-zhivet.orgnarod.su.cfg \
    -extensions v3_req
```

#### Мессенджер

```bash
openssl genrsa -out messenger.orgnarod.su.key 4096

openssl req -new -key messenger.orgnarod.su.key -out messenger.orgnarod.su.csr -config messenger.orgnarod.su.cfg
```

Подпись корневым:
```bash
openssl x509 -req -in messenger.orgnarod.su.csr \
    -CA orgnarod.crt -CAkey orgnarod.key \
    -CAcreateserial \
    -out messenger.orgnarod.su.crt \
    -days 365 \
    -sha256 \
    -extfile messenger.orgnarod.su.cfg \
    -extensions v3_req
```


## Основное

### Бэкенд

Проверка доступа до :
```bash
docker compose up -d --build backend
```
