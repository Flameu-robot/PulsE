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