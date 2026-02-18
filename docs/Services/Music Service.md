#### 4. Media Service (Music)

Модуль для работы с тяжелым контентом.

- **Ответственность:**
    - Загрузка и конвертация треков (ffmpeg integration).
    - Каталогизация (альбомы, исполнители).
    - Генерация ссылок на прослушивание (HLS).
    - Учет прослушиваний (аналитика).
- **Технологии:**
    - Spring Cloud Stream (для асинхронной обработки задач конвертации).
    - MinIO Client SDK.
- **Хранение данных:**
    - Таблицы: `tracks`, `albums`, `playlists`, `user_library`.
    - MinIO: Аудио файлы (raw и сегментированные HLS).
- **Интеграция:**
    - **Входящие:** Событие `UserCreated` (создать пустую библиотеку).
    - **API:** React запрашивает `GET /tracks/{id}/stream` -> Сервис отдает `m3u8` плейлист.

### 4.1. Каталог и Музыкальные сущности (Core Catalog)

#### Таблица: `artists` (Исполнители)

Хранит информацию о музыкантах.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|Primary Key (PK).|
|**name**|`VARCHAR(255)`|Имя исполнителя / Название группы.|
|**avatar_url**|`VARCHAR(500)`|Ссылка на фото исполнителя (MinIO).|
|**bio**|`TEXT`|Биография.|
|**monthly_listeners**|`INTEGER`|Количество прослушиваний в месяц (кэш).|
|**verified**|`BOOLEAN`|Верифицирован ли профиль.|
|**created_at**|`TIMESTAMPTZ`|Дата добавления в каталог.|

#### Таблица: `albums** (Альбомы)

Сборники треков (LP, EP, Синглы).

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**artist_id**|`BIGINT`|FK на `artists`.|
|**title**|`VARCHAR(255)`|Название альбома.|
|**cover_url**|`VARCHAR(500)`|Обложка альбома.|
|**release_date**|`DATE`|Дата релиза.|
|**type**|`VARCHAR(20)`|Тип: `ALBUM`, `SINGLE`, `EP`.|
|**total_tracks**|`INTEGER`|Количество треков.|

#### Таблица: `tracks** (Треки)

Основная сущность.

| Поле                  | Тип данных     | Описание                                      |
| --------------------- | -------------- | --------------------------------------------- |
| **id**                | `BIGSERIAL`    | PK.                                           |
| **album_id**          | `BIGINT`       | FK на `albums` (может быть NULL, если сингл). |
| **artist_id**         | `BIGINT`       | FK на `artists`.                              |
| **title**             | `VARCHAR(255)` | Название трека.                               |
| **duration_ms**       | `INTEGER`      | Длительность в миллисекундах.                 |
| **file_path**         | `VARCHAR(500)` | **Путь к файлу** в MinIO (исходник).          |
| **hls_manifest_path** | `VARCHAR(500)` | Путь к `.m3u8` файлу для стриминга (HLS).     |
| **genre**             | `VARCHAR(50)`  | Жанр.                                         |
| **listen_count**      | `BIGINT`       | Общее число прослушиваний.                    |
| **created_at**        | `TIMESTAMPTZ`  | Дата загрузки.                                |

---

### 4.2. Пользовательский контент (User Content)

#### Таблица: `playlists** (Плейлисты)

Пользовательские подборки.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**owner_id**|`BIGINT`|ID пользователя (Identity Service).|
|**name**|`VARCHAR(100)`|Название ("Мой рок").|
|**description**|`TEXT`|Описание.|
|**cover_url**|`VARCHAR(500)`|Обложка плейлиста.|
|**is_public**|`BOOLEAN`|Публичный или приватный.|
|**tracks_count**|`INTEGER`|Количество треков (кэш).|
|**created_at**|`TIMESTAMPTZ`|Дата создания.|

#### Таблица: `playlist_tracks**

Связующая таблица (Many-to-Many) для порядка треков в плейлисте.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**playlist_id**|`BIGINT`|FK на `playlists`.|
|**track_id**|`BIGINT`|FK на `tracks`.|
|**position**|`INTEGER`|Порядковый номер трека в плейлисте.|
|**added_at**|`TIMESTAMPTZ`|Когда добавлен.|

#### Таблица: `user_favourites** (Библиотека)

"Любимые треки" пользователя (аналог лайков в музыке).

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**user_id**|`BIGINT`|ID пользователя.|
|**track_id**|`BIGINT`|FK на `tracks`.|
|**added_at**|`TIMESTAMPTZ`|Дата добавления.|
|_Constraint_||Уникальность: `(user_id, track_id)`.|

---

### 4.3. Стриминг и Аналитика (Streaming)

#### Таблица: `listening_history**

История прослушиваний. Нужна для рекомендаций и отображения "Недавние".

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**user_id**|`BIGINT`|Кто слушал.|
|**track_id**|`BIGINT`|Что слушал.|
|**listened_at**|`TIMESTAMPTZ`|Когда слушал.|
|** listened_duration_ms**|`INTEGER`|Сколько прослушал (до конца или бросил).|

#### Таблица: `stream_events** (Очередь аналитики)

Оптимизированная таблица для записи факта стриминга (партиционируется по дате).

| Поле            | Тип данных    | Описание                               |
| --------------- | ------------- | -------------------------------------- |
| **id**          | `BIGSERIAL`   | PK.                                    |
| **user_id**     | `BIGINT`      | ID юзера.                              |
| **track_id**    | `BIGINT`      | ID трека.                              |
| **event_time**  | `TIMESTAMPTZ` | Время события.                         |
| **quality**     | `VARCHAR(10)` | Качество потока (Low, High, Lossless). |
| **client_type** | `VARCHAR(20)` | Web, Mobile, Desktop.                  |