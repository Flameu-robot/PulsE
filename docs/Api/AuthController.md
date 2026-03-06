### 1.1 Регистрация

text

```
POST /api/auth/register
```

**Что делает:** Создаёт нового пользователя. Возвращает токены (access + refresh), чтобы сразу после регистрации пользователь был залогинен.

**Нужна авторизация:** Нет

**Что отправить (body, JSON):**

JSON

```
{
  "username": "vasya_pupkin",
  "email": "vasya@mail.ru",
  "password": "SuperSecret123!"
}
```

**Что придёт в ответ (201 Created):**

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
const response = await fetch('/api/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'vasya_pupkin',
    email: 'vasya@mail.ru',
    password: 'SuperSecret123!'
  })
});

const data = await response.json();
// Сохраняем токены
localStorage.setItem('accessToken', data.accessToken);
localStorage.setItem('refreshToken', data.refreshToken);
```

---

### 1.2 Вход (логин)

text

```
POST /api/auth/login
```

**Что делает:** Проверяет логин/пароль. Если всё ок — возвращает токены.

**Нужна авторизация:** Нет

**Что отправить:**

JSON

```
{
  "username": "vasya_pupkin",
  "password": "SuperSecret123!"
}
```

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
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'vasya_pupkin',
    password: 'SuperSecret123!'
  })
});

const data = await response.json();
localStorage.setItem('accessToken', data.accessToken);
localStorage.setItem('refreshToken', data.refreshToken);
```

---

### 1.3 Обновление токенов

text

```
POST /api/auth/refresh
```

**Что делает:** Access-токен живёт недолго (например, 15 минут). Когда он протухает, ты НЕ заставляешь пользователя заново вводить пароль. Вместо этого отправляешь refresh-токен и получаешь новую пару токенов.

**Нужна авторизация:** Нет (ты же не можешь авторизоваться — токен протух)

**Что отправить:**

JSON

```
{
  "refreshToken": "c3d2e1f0-a1b2-c3d4-e5f6-789012345678"
}
```

**Что придёт в ответ (200 OK):**

JSON

```
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...(НОВЫЙ)",
  "refreshToken": "новый-рефреш-токен-тоже-может-обновиться"
}
```

**Как использовать на фронте:**

JavaScript

```
const response = await fetch('/api/auth/refresh', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    refreshToken: localStorage.getItem('refreshToken')
  })
});

const data = await response.json();
// Перезаписываем токены на новые
localStorage.setItem('accessToken', data.accessToken);
localStorage.setItem('refreshToken', data.refreshToken);
```

**Когда вызывать:** Когда любой запрос вернул 401 (Unauthorized). Схема такая:

1. Делаешь запрос → получил 401
2. Вызываешь `/refresh`
3. Получил новые токены → повторяешь оригинальный запрос
4. Если и `/refresh` вернул ошибку → кидаешь на страницу логина

---

### 1.4 Выход (логаут)

text

```
POST /api/auth/logout
```

**Что делает:** Убивает ОДНУ конкретную сессию (один refresh-токен). Пользователь вылетает только с текущего устройства.

**Нужна авторизация:** ДА (нужен access-токен в заголовке)

**Что отправить:**

JSON

```
{
  "refreshToken": "c3d2e1f0-a1b2-c3d4-e5f6-789012345678"
}
```

**Что придёт в ответ (204 No Content):** Пустое тело. Просто статус 204 — значит всё ок.

**Как использовать на фронте:**

JavaScript

```
await fetch('/api/auth/logout', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  },
  body: JSON.stringify({
    refreshToken: localStorage.getItem('refreshToken')
  })
});

// Чистим всё локально
localStorage.removeItem('accessToken');
localStorage.removeItem('refreshToken');
// Редирект на страницу логина
window.location.href = '/login';
```

---

### 1.5 Выход со всех устройств

text

```
POST /api/auth/logout-all
```

**Что делает:** Убивает ВСЕ сессии пользователя. Если он залогинен на телефоне, планшете и компе — вылетит отовсюду.

**Нужна авторизация:** ДА

**Что отправить:** Ничего (тело запроса пустое)

**Что придёт в ответ (204 No Content):** Пустое тело.

**Как использовать на фронте:**

JavaScript

```
await fetch('/api/auth/logout-all', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

localStorage.removeItem('accessToken');
localStorage.removeItem('refreshToken');
window.location.href = '/login';
```

---

### 1.6 Получить текущего пользователя

text

```
GET /api/auth/me
```

**Что делает:** Возвращает информацию о том, кто сейчас залогинен. По сути — «кто я?».

**Нужна авторизация:** ДА

**Что отправить:** Ничего, просто GET-запрос.

**Что придёт в ответ (200 OK):**

JSON

```
{
  "id": 42,
  "username": "vasya_pupkin",
  "email": "vasya@mail.ru",
  "emailVerified": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Как использовать на фронте:**

JavaScript

```
const response = await fetch('/api/auth/me', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

const user = await response.json();
console.log(`Привет, ${user.username}!`);
```

---

### 1.7 Смена пароля

text

```
POST /api/auth/password/change
```

**Что делает:** Меняет пароль. Нужно знать старый пароль (чтобы кто попало не сменил).

**Нужна авторизация:** ДА

**Что отправить:**

JSON

```
{
  "oldPassword": "SuperSecret123!",
  "newPassword": "EvenMoreSecret456!"
}
```

**Что придёт в ответ (204 No Content):** Пустое тело.

**Как использовать на фронте:**

JavaScript

```
const response = await fetch('/api/auth/password/change', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  },
  body: JSON.stringify({
    oldPassword: 'SuperSecret123!',
    newPassword: 'EvenMoreSecret456!'
  })
});

if (response.status === 204) {
  alert('Пароль успешно изменён!');
}
```