-- chatId в Telegram приходит в виде Long, меняем тип столбца
alter table manager alter column tg_channel_id type bigint;
