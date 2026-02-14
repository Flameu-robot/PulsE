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