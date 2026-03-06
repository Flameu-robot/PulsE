#### 1. Identity Service (Фундамент)

Это "сердце" системы. Его разрабатывают первым.

- **Ответственность:**
    - Регистрация, вход (OAuth2, Login/Password, Passkeys/WebAuthn).
    - Управление сессиями (JWT Access/Refresh tokens).
    - Хранение профилей (username, avatar, status).
    - RBAC (Role-Based Access Control) — права админов, модераторов, пользователей.
- **Технологии:**
    - Spring Security 6 + `spring-boot-starter-oauth2-resource-server`.
    - Библиотека `webauthn-server-attestation` для Passkeys.
- **Хранение данных:**
    - Таблицы: `users`, `roles`, `user_credentials`, `oauth2_tokens`.
- **Интеграция (Зависимости):**
    - **Входящие:** Запросы на авторизацию от API Gateway.
    - **Исходящие (Events):**
        - `UserCreated` -> Kafka (нужно, чтобы создать профиль в Media Service).
        - `UserBanned` -> Kafka (нужно, чтобы отключить его от Messaging и Spaces).
        - `UserUpdated` -> Kafka (обновить имя в чатах).
    - **Исходящие (Sync):** Валидация JWT токенов (PublicKey раскрыт на Gateway, проверка локальная).


### **Сущности:**
#### 1) Таблица: `Users`

Главная таблица профиля. Не хранит секретов (паролей), только публичную информацию и служебные метки.

| Поле              | Тип данных     | Описание                                                                     |
| ----------------- | -------------- | ---------------------------------------------------------------------------- |
| **id**            | `BIGSERIAL`    | Primary Key (PK). Уникальный ID пользователя.                                |
| **username**      | `VARCHAR(50)`  | Уникальный никнейм (например, `alex_dev`). Индекс.                           |
| **email**         | `VARCHAR(255)` | Email адрес. Уникальный. Индекс.                                             |
| **phone**         | `VARCHAR(20)`  | Телефон (опционально). Уникальный.                                           |
| **password_hash** | `VARCHAR(255)` | Хеш пароля (BCrypt). Может быть NULL, если вход только через OAuth/Passkeys. |
| **status**        | `VARCHAR(20)`  | Статус аккаунта: `ACTIVE`, `BANNED`, `DELETED`, `PENDING`.                   |
| **role**          | `VARCHAR(20)`  | Глобальная роль: `USER`, `ADMIN`, `MODERATOR`.                               |
| **created_at**    | `TIMESTAMPTZ`  | Дата регистрации.                                                            |
| **last_login_at** | `TIMESTAMPTZ`  | Время последнего входа.                                                      |
| **avatar_url**    | `VARCHAR(500)` | Ссылка на аватар в S3 (MinIO).                                               |
| **bio**           | `VARCHAR(500)` | Короткое описание "О себе".                                                  |
| **metadata**      | `JSONB`        | Гибкие данные: настройки темы, язык, соц. ссылки.                            |

### 2) Таблица: WebAuth

Хранит зарегистрированные устройства пользователя (FaceID, YubiKey, TouchID).

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**user_id**|`BIGINT`|FK на таблицу `users`.|
|**credential_id**|`BYTEA`|Уникальный ID ключа (закодированный). Индекс.|
|**public_key**|`BYTEA`|Публичный ключ устройства.|
|**sign_count**|`BIGINT`|Счетчик использования (защита от клонирования ключа).|
|**transports**|`VARCHAR(50)`|Тип транспорта (USB, NFC, INTERNAL, HYBRID).|
|**aaguid**|`UUID`|ID модели устройства (например, это iPhone или ноутбук).|
|**device_name**|`VARCHAR(100)`|Читаемое имя: "MacBook Pro", "iPhone 15".|
|**created_at**|`TIMESTAMPTZ`|Дата привязки устройства.|

### 3) Таблица: OAuth2 

Если пользователь входит через Google/GitHub.

| Поле            | Тип данных     | Описание                       |
| --------------- | -------------- | ------------------------------ |
| **id**          | `BIGSERIAL`    | PK.                            |
| **user_id**     | `BIGINT`       | FK на таблицу `users`.         |
| **provider**    | `VARCHAR(20)`  | Провайдер: `GOOGLE`, `GITHUB`. |
| **provider_id** | `VARCHAR(255)` | ID пользователя у провайдера.  |
| **linked_at**   | `TIMESTAMPTZ`  | Дата привязки.                 |

#### 4) Таблица: `Tokens`

Хранение долгоживущих токенов для обновления JWT. Нужно для реализации "Log out from all devices".

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**user_id**|`BIGINT`|FK на таблицу `users`.|
|**token_hash**|`VARCHAR(255)`|Хеш Refresh Token (не сам токен!).|
|**is_revoked**|`BOOLEAN`|Отозван ли токен (при логауте).|
|**expires_at**|`TIMESTAMPTZ`|Время жизни токена.|
|**created_at**|`TIMESTAMPTZ`|Дата создания.|
|**user_agent**|`VARCHAR(255)`|Информация о браузере/устройстве (для истории).|
|**ip_address**|`INET`|IP адрес создания сессии.|

### 5)  Таблица: `Verification`

Для подтверждения Email или сброса пароля.

|Поле|Тип данных|Описание|
|---|---|---|
|**id**|`BIGSERIAL`|PK.|
|**user_id**|`BIGINT`|FK на таблицу `users`.|
|**token**|`VARCHAR(255)`|Уникальный токен верификации. Индекс.|
|**type**|`VARCHAR(20)`|Тип: `EMAIL_VERIFY`, `PASSWORD_RESET`.|
|**expires_at**|`TIMESTAMPTZ`|Время жизни токена.|
|**used**|`BOOLEAN`|Был ли токен использован.|

### Связи и Индексы (Пояснение)

1. **Связи:**
    
    - `webauth.user_id` -> `users.id` (One-to-Many: У одного юзера много устройств).
    - `oauth2.user_id` -> `users.id` (One-to-Many).
    - `tokens.user_id` -> `users.id` (One-to-Many).
2. **Индексы:**
    
    - Обязательные уникальные индексы на: `username`, `email`.
    - Индекс на `refresh_tokens.token_hash` для быстрого поиска при обновлении токена.
    - Индекс на `verification_tokens.token` для быстрой проверки ссылки из письма.