

|Аспект|Детали|
|---|---|
|**Регистрация требует JWT**|Пользователь сначала должен залогиниться обычным способом, потом добавить ключ|
|**Вход не требует JWT**|`/login/start` и `/login/finish` публичные — это альтернатива паролю|
|**flowId одноразовый**|После `getAndRemove()` в Redis данных нет — replay невозможен|
|**TTL = 5 минут**|Если пользователь не завершил за 5 минут — начинать заново|
|**signCount**|Растёт при каждом использовании. Если пришёл меньший — ключ склонирован|
|**Passwordless**|Если не передать `username` в `/login/start` — браузер сам найдёт discoverable credentials|

### 1. Регистрация ключа (пользователь уже залогинен)

#### Шаг 1: `POST /api/auth/webauthn/register/start`

**Кто вызывает:** Авторизованный пользователь хочет добавить security key.

**Запрос:**

http

```
POST /api/auth/webauthn/register/start
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Что происходит в `startRegistration()`:**

1. Достаём username из JWT токена (`@AuthenticationPrincipal`)
2. Генерируем challenge (32 случайных байта)
3. Формируем опции для браузера (rpId, userId, алгоритмы)
4. Сохраняем challenge в Redis с TTL 5 минут → получаем `flowId`

**Ответ - json**

**Фронтенд получает этот JSON и вызывает:**

```
const options = response.publicKeyCredentialCreationOptions;

// Декодируем base64 → ArrayBuffer
options.challenge = base64ToArrayBuffer(options.challenge);
options.user.id = base64ToArrayBuffer(options.user.id);

// Вызываем браузерный API — появляется системный диалог
const credential = await navigator.credentials.create({
    publicKey: options
});
```


#### Шаг 2: `POST /api/auth/webauthn/register/finish`

**Запрос:**

http

```
POST /api/auth/webauthn/register/finish?flowId=550e8400-...&deviceName=MacBook%20Pro
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "id": "Aej3...credential-id-base64...",
  "rawId": "Aej3...same-base64...",
  "type": "public-key",
  "response": {
    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIi...base64...",
    "attestationObject": "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVi...base64..."
  }
}
```

**Что происходит в `finishRegistration()`:**

1. Достаём сохранённый challenge из Redis по `flowId` (и удаляем — одноразовый)
2. Парсим `credentialJson` через Yubico библиотеку
3. Проверяем:
    - Challenge в `clientDataJSON` совпадает с сохранённым
    - Origin (`http://localhost:3000`) в списке разрешённых
    - Подпись валидна
4. Извлекаем `publicKey` и `credentialId`
5. Сохраняем в БД: `WebAuthnCredential(user, credentialId, publicKey, signCount=0)`

**Ответ:**

JSON

```
{
  "message": "WebAuthn credential registered"
}
```


### 2. Вход по ключу (пользователь НЕ залогинен)

#### Шаг 1: `POST /api/auth/webauthn/login/start`

**Вариант A — с указанием username:**

http

```
POST /api/auth/webauthn/login/start?username=testuser
```

**Вариант B — passwordless (без username):**

http

```
POST /api/auth/webauthn/login/start
```

**Ответ (вариант A — сервер знает какие ключи у пользователя):**

JSON

```
{
  "flowId": "660e8400-e29b-41d4-a716-446655440001",
  "assertionRequest": {
    "challenge": "YW5vdGhlci1jaGFsbGVuZ2UtYmFzZTY0...",
    "rpId": "localhost",
    "timeout": 60000,
    "allowCredentials": [
      {
        "type": "public-key",
        "id": "Aej3...credential-id...",
        "transports": ["usb", "internal"]
      }
    ],
    "userVerification": "preferred"
  }
}
```

**Ответ (вариант B — passwordless):**

JSON

```
{
  "flowId": "660e8400-...",
  "assertionRequest": {
    "challenge": "...",
    "rpId": "localhost",
    "allowCredentials": null  // браузер сам найдёт ключи для этого rpId
  }
}
```

**Фронтенд:**

JavaScript

```
const options = response.assertionRequest;
options.challenge = base64ToArrayBuffer(options.challenge);

if (options.allowCredentials) {
    options.allowCredentials = options.allowCredentials.map(c => ({
        ...c,
        id: base64ToArrayBuffer(c.id)
    }));
}

// Браузер покажет диалог с выбором ключа
const assertion = await navigator.credentials.get({
    publicKey: options
});

// Аутентификатор подписывает challenge приватным ключом
```

---

#### Шаг 2: `POST /api/auth/webauthn/login/finish`

**Запрос:**

http

```
POST /api/auth/webauthn/login/finish?flowId=660e8400-...
Content-Type: application/json

{
  "id": "Aej3...credential-id...",
  "rawId": "Aej3...",
  "type": "public-key",
  "response": {
    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0Ii...base64...",
    "authenticatorData": "SZYN5YgOjGh0NBcPZHZg...base64...",
    "signature": "MEUCIQDpe3...base64...",
    "userHandle": "AAAAAAAAAAE="  // только для passwordless
  }
}
```

**Что происходит в `finishAuthentication()`:**

1. Достаём challenge из Redis по `flowId`
2. По `credentialId` находим `WebAuthnCredential` в БД
3. Проверяем подпись с помощью сохранённого `publicKey`
4. Проверяем `signCount` — должен быть больше сохранённого (защита от клонирования)
5. Обновляем `signCount` в БД
6. Генерируем JWT токены через `AuthResponseFactory`

**Ответ:**

JSON

```
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "testuser",
  "role": "USER"
}
```

### 3. Удаление ключа

http

```
DELETE /api/auth/webauthn/credentials/10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Проверки:**

- Ключ существует
- Ключ принадлежит текущему пользователю
- Это не единственный способ входа (есть пароль или другие ключи)

**Ответ:** `204 No Content`