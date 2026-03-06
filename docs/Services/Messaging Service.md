#### 2. Messaging Service (Чаты)

Высоконагруженный модуль. Зависит от Identity (получает UserID из токена).

- **Ответственность:**
    - Создание чатов (Private, Group, Channel).
    - CRUD сообщений.
    - Реал-тайм транспорт (WebSocket).
    - Реакции, Ответы, Закрепы.
    - Полнотекстовый поиск.
- **Технологии:**
    - Spring WebSocket (STOMP).
    - Spring Data JPA + Hibernate.
    - Elasticsearch (интеграция через Spring Data Elasticsearch).
    - Redis (для хранения "кто печатает" и кэша последних сообщений).
- **Хранение данных:**
    - Таблицы: `chats`, `chat_members`, `messages`.
    - MinIO: Вложения (фото, документы).
- **Интеграция:**
    - **Входящие:** JWT Token из заголовка (проверяет, что юзер валидный).
    - **События (In):** Слушает `UserBanned` (чтобы заблокировать юзера в чатах).
    - **События (Out):**
        - `MessageSent` -> Kafka (нужно для Notification Gateway, чтобы отправить пуш).
        - `MediaUploaded` -> (ссылается на Media Service или MinIO).

### 2.1. Структура и Организация (Groups & Channels)

#### Таблица: `groups` (Центральная сущность)

Это Unified Group Entity. Здесь живут личные чаты, каналы, группы и Discord-сервера.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|Primary Key (PK).|
|**owner_id**|`BIGINT`|ID создателя (FK на Identity Service).|
|**name**|`VARCHAR(100)`|Название группы/канала/сервера.|
|**type**|`VARCHAR(20)`|Тип: `PERSONAL` (ЛС), `GROUP` (Группа), `CHANNEL` (Канал).|
|**features**|`JSONB`|**Ключевое поле.** Функциональные флаги.  <br>Пример: `{"voice_enabled": true, "roles_enabled": false}`.|
|**avatar_url**|`VARCHAR(500)`|Ссылка на аватар группы.|
|**description**|`TEXT`|Описание (био группы).|
|**created_at**|`TIMESTAMPTZ`|Дата создания.|
|**updated_at**|`TIMESTAMPTZ`|Дата обновления.|

#### Таблица: `group_members` (Участники и Права)

Связывает пользователей с группами. Отвечает за списки участников и права доступа.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**group_id**|`BIGINT`|FK на таблицу `groups`.|
|**user_id**|`BIGINT`|ID пользователя (Identity Service).|
|**role**|`VARCHAR(20)`|Роль: `OWNER`, `ADMIN`, `MEMBER`, `SUBSCRIBER` (для каналов).|
|**joined_at**|`TIMESTAMPTZ`|Дата вступления.|
|**muted**|`BOOLEAN`|Выключены ли уведомления для юзера.|
|**last_read_message_id**|`BIGINT`|ID последнего прочитанного сообщения (для счетчиков непрочитанных).|

#### Таблица: `text_channels` (Ветки внутри Группы)

Если Группа имеет `type = GROUP` или `CHANNEL`, и `features.roles_enabled = true` (Discord Mode), то текстовые каналы лежат здесь. Если это простой чат, эта таблица не используется.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**group_id**|`BIGINT`|FK на таблицу `groups`.|
|**name**|`VARCHAR(50)`|Название канала (например, "general").|
|**topic**|`VARCHAR(200)`|Топик/описание канала.|
|**position**|`INTEGER`|Порядок сортировки в списке.|

---

### 2.2. Контент (Messages)

#### Таблица: `messages`

Основное хранилище всех сообщений системы.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**channel_id**|`BIGINT`|FK на `text_channels`. Если это простой чат без каналов — ссылка на дефолтный "hidden" канал или NULL (зависит от реализации, лучше всегда ссылаться на канал).|
|**sender_id**|`BIGINT`|ID отправителя (Identity).|
|**content**|`TEXT`|Текст сообщения. Может быть NULL, если это только медиа.|
|**metadata**|`JSONB`|Дополнительные данные: `{ "edited": true, "edited_at": "..." }`.|
|**reply_to_id**|`BIGINT`|FK на саму таблицу `messages` (Ответ на сообщение).|
|**forward_from_id**|`BIGINT`|ID оригинального сообщения (Пересылка).|
|**created_at**|`TIMESTAMPTZ`|Время отправки. Индекс.|

#### Таблица: `message_attachments`

Хранение ссылок на файлы (фото, видео, документы) внутри сообщения.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**message_id**|`BIGINT`|FK на `messages`.|
|**type**|`VARCHAR(20)`|Тип: `IMAGE`, `VIDEO`, `FILE`, `AUDIO`.|
|**url**|`VARCHAR(500)`|Ссылка на MinIO/S3.|
|**preview_url**|`VARCHAR(500)`|Ссылка на превью (для видео/картинок).|
|**filename**|`VARCHAR(255)`|Имя файла.|
|**size**|`BIGINT`|Размер в байтах.|

---

### 2.3. Социальный граф (Feed & Friends)

Нужен для работы Ленты (Feed Service) и списка друзей.

#### Таблица: `user_relations`

Связи между пользователями: Друзья, Подписчики, Блокировки.

| Поле             | Тип данных    | Описание                                                         |
| ---------------- | ------------- | ---------------------------------------------------------------- |
| **id**           | `BIGSERIAL`   | PK.                                                              |
| **requester_id** | `BIGINT`      | Кто отправил запрос/подписался.                                  |
| **receiver_id**  | `BIGINT`      | Кому отправили.                                                  |
| **status**       | `VARCHAR(20)` | Статус: `PENDING`, `ACCEPTED` (Друзья), `SUBSCRIBER`, `BLOCKED`. |
| **created_at**   | `TIMESTAMPTZ` | Дата создания связи.                                             |

---

### 2.4. Взаимодействия (Reactions & Pins)

#### Таблица: `message_reactions`

Эмодзи реакции под сообщениями.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**message_id**|`BIGINT`|FK на `messages`.|
|**user_id**|`BIGINT`|Кто поставил реакцию.|
|**emoji**|`VARCHAR(10)`|Unicode символ эмодзи или код.|
|**created_at**|`TIMESTAMPTZ`|Дата.|
|_Constraint_||Уникальность: `(message_id, user_id, emoji)` — чтобы нельзя было дважды поставить одно и то же.|

#### Таблица: `pinned_messages`

Закрепленные сообщения в каналах/чатах.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**channel_id**|`BIGINT`|FK на `text_channels`.|
|**message_id**|`BIGINT`|FK на `messages`.|
|**pinned_by**|`BIGINT`|Кто закрепил.|
|**pinned_at**|`TIMESTAMPTZ`|Когда закрепили.|
1. **Telegram-like:** Используем `groups` с `type=PERSONAL` или `CHANNEL`. Используем `messages`.
2. **Discord-like:** Используем `groups` с `features={roles_enabled: true}` и таблицу `text_channels` для создания структуры каналов внутри группы.
3. **Feed:** Таблица `user_relations` позволяет формировать ленту постов от друзей/подписок.