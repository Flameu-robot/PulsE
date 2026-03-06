**WebAuthn** — это вход по отпечатку пальца, Face ID, USB-ключу (YubiKey) и т.д.  
Работает в два шага: «начни» (start) → «заверши» (finish).

### 6.1 Начать регистрацию ключа

text

```
POST /api/auth/webauthn/register/start
```

**Что делает:** Говорит серверу «я хочу добавить аппаратный ключ/отпечаток». Сервер возвращает JSON с параметрами, которые нужно передать в браузерный API `navigator.credentials.create()`.

**Нужна авторизация:** ДА (ты уже залогинен и добавляешь ключ как доп. метод входа)

**Что отправить:** Ничего.

**Что придёт в ответ (200 OK):** Большой JSON с параметрами для WebAuthn (challenge, rpId и т.д.), плюс `flowId` — идентификатор этого процесса.

**Как использовать на фронте:**

JavaScript

```
// Шаг 1: Получаем параметры с сервера
const response = await fetch('/api/auth/webauthn/register/start', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

const options = await response.json();
// options содержит flowId и publicKeyCredentialCreationOptions

// Шаг 2: Вызываем браузерный API (появится диалог "приложите палец" и т.д.)
const credential = await navigator.credentials.create({
  publicKey: options.publicKeyCredentialCreationOptions
});

// Шаг 3: Отправляем результат на сервер (см. следующий эндпоинт)
```

---

### 6.2 Завершить регистрацию ключа

text

```
POST /api/auth/webauthn/register/finish?flowId=xxx&deviceName=My YubiKey
```

**Что делает:** Принимает ответ от браузера (credential) и сохраняет ключ в базе.

**Нужна авторизация:** ДА

**Что отправить:**

- Query-параметр `flowId` — получили на предыдущем шаге
- Query-параметр `deviceName` (необязательный) — человеко-читаемое название, например «Мой YubiKey»
- Body — JSON-строка credential от браузера

**Что придёт в ответ (201 Created):**

JSON

```
{
  "message": "WebAuthn credential registered"
}
```

**Как использовать на фронте:**

JavaScript

```
// credential — получили из navigator.credentials.create() на предыдущем шаге
const credentialJson = JSON.stringify(credential);

await fetch(
  `/api/auth/webauthn/register/finish?flowId=${options.flowId}&deviceName=My YubiKey`,
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
    },
    body: credentialJson
  }
);

alert('Ключ зарегистрирован!');
```

---

### 6.3 Начать вход по ключу

text

```
POST /api/auth/webauthn/login/start?username=vasya_pupkin
```

**Что делает:** Говорит серверу «хочу войти через WebAuthn». Сервер возвращает challenge для `navigator.credentials.get()`. Параметр `username` необязательный — если не указать, браузер сам покажет доступные ключи.

**Нужна авторизация:** Нет (ты пытаешься войти)

**Как использовать на фронте:**

JavaScript

```
// Шаг 1: Получаем challenge
const response = await fetch('/api/auth/webauthn/login/start?username=vasya_pupkin', {
  method: 'POST'
});

const options = await response.json();

// Шаг 2: Вызываем браузерный API (появится диалог "приложите палец")
const assertion = await navigator.credentials.get({
  publicKey: options.publicKeyCredentialRequestOptions
});

// Шаг 3: Отправляем на сервер (см. следующий эндпоинт)
```

---

### 6.4 Завершить вход по ключу

text

```
POST /api/auth/webauthn/login/finish?flowId=xxx
```

**Что делает:** Проверяет ответ от браузера. Если ключ валидный — возвращает токены (как при обычном логине).

**Нужна авторизация:** Нет

**Что придёт в ответ (200 OK):**

JSON

```
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
  "refreshToken": "c3d2e1f0-a1b2-c3d4-e5f6-789012345678"
}
```

**Как использовать на фронте:**

JavaScript

```
const assertionJson = JSON.stringify(assertion);

const loginResponse = await fetch(
  `/api/auth/webauthn/login/finish?flowId=${options.flowId}`,
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: assertionJson
  }
);

const data = await loginResponse.json();
localStorage.setItem('accessToken', data.accessToken);
localStorage.setItem('refreshToken', data.refreshToken);
// Пользователь залогинен!
```

---

### 6.5 Удалить зарегистрированный ключ

text

```
DELETE /api/auth/webauthn/credentials/{id}
```

**Что делает:** Удаляет ранее зарегистрированный WebAuthn-ключ. Например, потерял YubiKey — удаляешь его из аккаунта.

**Нужна авторизация:** ДА

**Что отправить:** Ничего. ID ключа в URL.

**Что придёт в ответ (204 No Content):** Пустое тело.

**Как использовать на фронте:**

JavaScript

```
const credentialId = 5;

await fetch(`/api/auth/webauthn/credentials/${credentialId}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

alert('Ключ удалён!');
```