--tg_channel_id не нужен в таблице manager

alter table manager drop column tg_channel_id;

--tg_name unique в волонтерах нужен

alter table volunteer add constraint unique_volunteer unique (tg_name);