-- chatId в Telegram приходит в виде Long, меняем тип столбца
alter table volunteer alter column tg_channel_id type bigint;

-- Добавляем поле contact в таблицу task. Поле нужно, чтобы в боте оставили как связаться, если аккаунт тг закрыт
alter table task add column contact varchar(1000)
