-- Таблица админов (тг аутентификация, выписываем JWT токены, в базе ничо не храним)
create table manager (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    tg_name varchar(100) unique not null,
    tg_channel_id int unique not null,
    is_admin boolean not null
);

-- статус волонтера.
create type volunteer_status as enum('Active', 'InVacation');
-- Таблица волонтеров ("виртуальную создал" или мб будем заполнять такую процедурой или типа того)
create table volunteer (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    tg_name varchar(100) not null,
    tg_channel_id int not null,
    register_date timestamp without time zone not null,--!!!
    status volunteer_status not null,
    status_date timestamp without time zone not null,
    is_banned boolean not null,
    note varchar(1000),
    coordinates GEOMETRY,
    radius int
);

-- ТИП задачи
create type task_type as enum ('Social', 'Ecological');
-- СТАТУС задачи
create type task_status as enum ('New', 'InProcessing', 'InPlan', 'Done', 'Inactive', 'Archive');
-- ЗАДАЧИ
create table task (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type task_type not null,
    name varchar(150) not null, --размер?
    create_date timestamp without time zone not null,
    status task_status not null,
    status_date timestamp without time zone not null,
    event_datetime_utc timestamp without time zone,
    leader integer references manager, --ref тут надо донастроить защиту от удалений
    -- ведущий волонтер?
    chat_link varchar(150), --размер?
    note varchar(1000), --размер?
    coordinates GEOMETRY
);
-- детализация для соц задачи
create table soctask_info (
    task_id integer references task, --ref  тут надо донастроить защиту от удалений
    address varchar(200)
);

-- Таблица под все несистемные теги задач
create table users_tag (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name varchar(30) not null,
    task task_type not null,
    is_hidden_in_bot boolean not null,
    is_archived boolean not null,
    UNIQUE (name, task)
);
-- ЗАДАЧА - ТЭГИ
create table task_users_tag (
    task_id integer references task,
    tag_id integer references users_tag,
    UNIQUE (task_id, tag_id)
);
--// API: add(name, task_type, is_allowed_in_bot)
--//      hide(id, task_type, HIDDEN_SOURCE)

-- Таблица под системные теги
create table system_tag (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    task_id integer references task,
    name varchar(30) not null,
    UNIQUE (task_id, name)
);

-- эту витрину юзаем когда нужно получить юзерские тэги
create or replace view v_task_users_tags as
select
	tut.task_id, ut.id, name
from users_tag ut
	join task_users_tag tut on ut.id = tut.tag_id;

-- тут вычисляем системные теги
create or replace view v_task_system_tags as
select
	id as task_id,
	case
	    when
	        status not in ('InPlan', 'Done', 'Inactive', 'Archive') and
	        (
	            (event_datetime_utc is not null and
	            extract(day from (now()::timestamp without time zone) - event_datetime_utc) >= 2)
	            or
	            (event_datetime_utc is null and
	            extract(day from (now()::timestamp without time zone) - event_datetime_utc) >= 5)
	        )
	    then 'Просрочена'
	    else null
	end as name
from task;

-- ЗАДАЧА - ФОТО
create table task_photo (
    task_id integer references task,
    photo_id integer not null,
    photo_path varchar(30) not null, --4(+1)+2(+1)+2(+1)+11+(+1+3) = 30
    optimized_photo_path varchar(30) not null, --4(+1)+2(+1)+2(+1)+11+(+1+3) = 30
    UNIQUE(photo_id)
);

create sequence photo_id_sequence;

--
create table task_volunteer_done (
    task_id integer references task,
    volunteer_id integer references volunteer,
    UNIQUE(task_id, volunteer_id)
);
