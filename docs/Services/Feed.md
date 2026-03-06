### 1. Контент (Posts & Media)

#### Таблица: `posts`

Основная таблица ленты. Хранит текст, настройки и метаданные поста.

| Поле           | Тип данных    | Описание                                                            |
| -------------- | ------------- | ------------------------------------------------------------------- |
| **id**         | `BIGSERIAL`   | Primary Key (PK).                                                   |
| **author_id**  | `BIGINT`      | ID автора (FK на Identity Service). Индекс.                         |
| **content**    | `TEXT`        | Текст поста.                                                        |
| **visibility** | `VARCHAR(20)` | Видимость: `PUBLIC` (публичный), `FRIENDS` (для друзей), `PRIVATE`. |
| **is_pinned**  | `BOOLEAN`     | Закреплен ли пост в профиле.                                        |
| **metadata**   | `JSONB`       | Доп. данные: `{ "edit_history": [...], "location": "Moscow" }`.     |
| **created_at** | `TIMESTAMPTZ` | Время создания. Индекс.                                             |
| **updated_at** | `TIMESTAMPTZ` | Время последнего редактирования.                                    |

#### Таблица: `post_attachments`

Хранит ссылки на медиа-файлы в посте (фото, видео).

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**post_id**|`BIGINT`|FK на таблицу `posts`.|
|**media_type**|`VARCHAR(20)`|`IMAGE`, `VIDEO`, `MUSIC_LINK`.|
|**url**|`VARCHAR(500)`|Ссылка на файл (MinIO/S3).|
|**preview_url**|`VARCHAR(500)`|Превью (для видео).|
|**order_index**|`INTEGER`|Порядок картинок внутри поста.|
|**metadata**|`JSONB`|Ширина, высота, размер файла.|

#### Таблица: `post_music_links** (Интеграция с Media Service)

Специальная таблица для прикрепления треков из вашего музыкального сервиса.

| Поле             | Тип данных  | Описание                               |
| ---------------- | ----------- | -------------------------------------- |
| **id**           | `BIGSERIAL` | PK.                                    |
| **post_id**      | `BIGINT`    | FK на `posts`.                         |
| **track_id**     | `BIGINT`    | ID трека (из Media Service).           |
| **listen_count** | `INTEGER`   | Сколько раз прослушали из этого поста. |

---

### 2. Социальный граф (Social Graph)

#### Таблица: `follows`

Определяет, кто на кого подписан. Нужна для формирования ленты "Мои друзья/Подписки".

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**follower_id**|`BIGINT`|Кто подписался.|
|**followee_id**|`BIGINT`|На кого подписались.|
|**status**|`VARCHAR(20)`|`ACTIVE` (подписан), `PENDING` (заявка в друзья), `BLOCKED`.|
|**created_at**|`TIMESTAMPTZ`|Дата подписки.|
|_Constraint_||Уникальность: `(follower_id, followee_id)`.|

---

### 3. Взаимодействия (Interactions)

#### Таблица: `likes**

Хранит лайки к постам.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**post_id**|`BIGINT`|FK на `posts`.|
|**user_id**|`BIGINT`|Кто поставил лайк.|
|**created_at**|`TIMESTAMPTZ`|Дата лайка.|
|_Constraint_||Уникальность: `(post_id, user_id)`.|

#### Таблица: `comments**

Комментарии под постами. По сути, это мини-чат, привязанный к посту.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**post_id**|`BIGINT`|FK на `posts`. Индекс.|
|**author_id**|`BIGINT`|ID автора комментария.|
|**content**|`TEXT`|Текст комментария.|
|**parent_comment_id**|`BIGINT`|FK на саму таблицу `comments` (для ответов).|
|**created_at**|`TIMESTAMPTZ`|Дата.|

#### Таблица: `bookmarks** (Закладки)

Позволяет сохранять посты.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**user_id**|`BIGINT`|Кто сохранил.|
|**post_id**|`BIGINT`|Какой пост.|
|**created_at**|`TIMESTAMPTZ`|Дата сохранения.|

---

### 4. Агрегация (Analytics & Counts)

Для быстрой работы ленты (чтобы не считать `COUNT(*)` каждый раз) храним счетчики в отдельной таблице.

#### Таблица: `post_stats**

Статистика поста (денормализованные данные для скорости).

|Поле|Тип данных|Описание|
|---|---|---|
|**post_id**|`BIGINT`|PK, FK на `posts`.|
|**likes_count**|`INTEGER`|Количество лайков.|
|**comments_count**|`INTEGER`|Количество комментариев.|
|**shares_count**|`INTEGER`|Количество репостов.|
|**views_count**|`INTEGER`|Количество просмотров.|