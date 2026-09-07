
insert into manager values
    (default, '@dskozlov', 1, true),
    (default, '@aka_naked_gun', 2, false),
    (default, '@overln2', 3, false);

insert into volunteer values
    (default, '@dostoevski', 4, '2023-01-01T00:00:00'::timestamp, 'Active', '2023-01-01T00:00:00'::timestamp, false, 'Нравится социальная работа', point(73.36993, 54.98793)::geometry, 10000),
    (default, '@tolstoi',  5, '2022-01-01T00:00:00'::timestamp, 'Active', '2023-01-01T00:00:00'::timestamp, true, default, point(37.603241, 55.753391)::geometry, 1500),
    (default, '@bulgakov',  6, '2020-07-01T00:00:00'::timestamp, 'InVacation', '2021-01-01T00:00:00'::timestamp, true, 'Нравитя экологическая работа', point(37.603241, 55.753391)::geometry, 1500),
    (default, '@dskozlov', 1, '2017-07-17T00:00:00'::timestamp, 'Active', '2018-01-01T00:00:00'::timestamp, false, default, point(37.619003, 55.767254)::geometry, 2000),
    (default, '@overln2', 3, '2025-07-13T00:00:00'::timestamp, 'Active', '2025-07-14T00:00:00'::timestamp, false, default, point(37.634423, 55.767652)::geometry, 3000),
    (default, '@aka_naked_gun', 2, '2025-07-12T00:00:00'::timestamp, 'InVacation', '2025-07-14T00:00:00'::timestamp, false, default, point(37.639601, 55.756314)::geometry, 4000);

insert into users_tag values
    (default, 'Москва', 'Social', false, false),
    (default, 'Одинцово', 'Ecological', false, false),
    (default, 'Срочно', 'Ecological', false, false),
    (default, 'Свалка', 'Social', false, false),
    (default, 'Плохой тэг', 'Social', true, false),
    (default, 'Неприемлемый тэг', 'Ecological', false, true);

insert into task values
    (default,
     'Social',
     'Задача у Ленина',
     '2025-06-01T09:00:00'::timestamp,
     'New',
     '2025-06-01T09:00:00'::timestamp,
     '2025-06-07T09:00:00'::timestamp,
     (select id from manager where tg_name = '@dskozlov'),
     'https://t.me/TrueSql',
     'Большая сложная проблема',
     point(37.619875, 55.753674)::geometry
     ),
     (default,
     'Social',
     'Задача у Сретинского монастыря',
     '2025-06-09T09:00:00'::timestamp,
     'InProcessing',
     '2025-06-10T09:00:00'::timestamp,
     null,
     (select id from manager where tg_name = '@dskozlov'),
      'https://t.me/auantonov',
     default,
     point(37.629593, 55.765651)::geometry
     ),
     (default,
     'Ecological',
     'Превышение нормы выбросов',
     '2025-06-15T09:00:00'::timestamp,
     'InPlan',
     '2025-06-15T10:00:00'::timestamp,
     null,
     (select id from manager where tg_name = '@aka_naked_gun'),
      'https://t.me/artem_rakhmeev_notes',
     'Легкий вопрос',
     point(37.658338, 55.690620)::geometry
     ),
     (default,
     'Ecological',
     'Провести анализ экологической ситуации в битцевском лесу',
     '2025-06-20T09:00:00'::timestamp,
     'Done',
     '2025-07-01T00:00:00'::timestamp,
     null,
     (select id from manager where tg_name = '@dskozlov'),
     default,
     default,
     point(37.563008, 55.627943)::geometry
     ),
      (default,
     'Social',
     'Анализ социальная обстановки в раменках',
     '2025-06-21T09:00:00'::timestamp,
     'Inactive',
     '2025-07-10T00:00:00'::timestamp,
     null,
     (select id from manager where tg_name = '@overln2'),
      default,
     'Связаться с мерией',
     point(37.504529, 55.703976)::geometry
     ),
      (default,
     'Ecological',
     'Загрязнение реки в серебряном бору',
     '2025-06-25T09:00:00'::timestamp,
     'Archive',
     '2025-07-05T00:00:00'::timestamp,
     null,
     (select id from manager where tg_name = '@dskozlov'),
      default,
     default,
     point(37.425647, 55.781793)::geometry
     );

insert into task_users_tag values
    ((select id from task where name = 'Задача у Ленина'), (select id from users_tag where name = 'Москва')),
    ((select id from task where name = 'Задача у Ленина'), (select id from users_tag where name = 'Срочно')),
    ((select id from task where name = 'Задача у Ленина'), (select id from users_tag where name = 'Плохой тэг')),

    ((select id from task where name = 'Задача у Сретинского монастыря'), (select id from users_tag where name = 'Одинцово')),

-- FIXME: нельзя просто tag_id передать???
    ((select id from task where name = 'Превышение нормы выбросов'), (select id from users_tag where name = 'Москва')),
    ((select id from task where name = 'Превышение нормы выбросов'), (select id from users_tag where name = 'Одинцово')),

    ((select id from task where name = 'Провести анализ экологической ситуации в битцевском лесу'), (select id from users_tag where name = 'Одинцово')),
    ((select id from task where name = 'Провести анализ экологической ситуации в битцевском лесу'), (select id from users_tag where name = 'Срочно')),
    ((select id from task where name = 'Провести анализ экологической ситуации в битцевском лесу'), (select id from users_tag where name = 'Неприемлемый тэг'));

 insert into task_volunteer_done values
    ((select id from task where name = 'Провести анализ экологической ситуации в битцевском лесу'), (select id from volunteer where tg_name = '@aka_naked_gun')),

    ((select id from task where name = 'Загрязнение реки в серебряном бору'), (select id from volunteer where tg_name = '@dskozlov'));

insert into task_photo values
    (1, nextval('photo_id_sequence'), '2025/9/9/1.jpg', '2025/9/9/optimized_1.jpg'),
    (1, nextval('photo_id_sequence'), '2025/9/9/2.jpg', '2025/9/9/optimized_2.jpg');