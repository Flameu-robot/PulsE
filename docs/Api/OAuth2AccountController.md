### 5.1 Получить привязанные OAuth2-аккаунты

text

```
GET /api/users/me/oauth2
```

**Что делает:** Показывает, какие внешние аккаунты (Google, GitHub, Яндекс и т.д.) привязаны к текущему пользователю.

**Нужна авторизация:** ДА

**Что отправить:** Ничего.

**Что придёт в ответ (200 OK):**

JSON

```
[
  {
    "provider": "google",
    "providerEmail": "vasya@gmail.com",
    "linkedAt": "2024-01-10T12:00:00"
  },
  {
    "provider": "github",
    "providerEmail": "vasya@github.com",
    "linkedAt": "2024-01-12T15:30:00"
  }
]
```

**Как использовать на фронте:**

JavaScript

```
const response = await fetch('/api/users/me/oauth2', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

const accounts = await response.json();
accounts.forEach(acc => {
  console.log(`Привязан ${acc.provider}: ${acc.providerEmail}`);
});
```

---

### 5.2 Отвязать OAuth2-аккаунт

text

```
DELETE /api/users/me/oauth2/{provider}
```

**Что делает:** Отвязывает внешний аккаунт. Например, хочешь отвязать Google — отправляешь DELETE с provider = `google`.

**Нужна авторизация:** ДА

**Что отправить:** Ничего. Название провайдера в URL.

**Что придёт в ответ (204 No Content):** Пустое тело.

**Как использовать на фронте:**

JavaScript

```
await fetch('/api/users/me/oauth2/google', {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

alert('Google-аккаунт отвязан!');
```