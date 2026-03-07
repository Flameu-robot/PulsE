### 4.1 Получить свой профиль

text

```
GET /api/users/me
```

**Что делает:** То же самое, что `GET /api/auth/me` — возвращает данные текущего пользователя. (Да, есть два похожих эндпоинта — один в AuthController, другой здесь. Используй какой удобнее.)

**Нужна авторизация:** ДА

**Что придёт в ответ (200 OK):**

JSON

```
{
  "id": 42,
  "username": "vasya_pupkin",
  "email": "vasya@mail.ru",
  "emailVerified": true,
  "displayName": "Вася Пупкин",
  "avatarUrl": "https://example.com/avatars/42.jpg",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Как использовать на фронте:**

JavaScript

```
const response = await fetch('/api/users/me', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

const profile = await response.json();
```

---

### 4.2 Обновить свой профиль

text

```
PATCH /api/users/me
```

**Что делает:** Обновляет данные профиля (например, отображаемое имя, аватарку). Можно отправить только те поля, которые хочешь поменять — необязательно все сразу.

**Нужна авторизация:** ДА

**Что отправить:**

JSON

```
{
  "displayName": "Василий Великий",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**Что придёт в ответ (200 OK):** Обновлённый профиль целиком.

**Как использовать на фронте:**

JavaScript

```
const response = await fetch('/api/users/me', {
  method: 'PATCH',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  },
  body: JSON.stringify({
    displayName: 'Василий Великий'
  })
});

const updatedProfile = await response.json();
```

---

### 4.3 Удалить свой аккаунт

text

```
DELETE /api/users/me
```

**Что делает:** Полностью удаляет аккаунт пользователя. Необратимо. Ставь подтверждение «Вы точно уверены?» на фронте.

**Нужна авторизация:** ДА

**Что отправить:** Ничего.

**Что придёт в ответ (204 No Content):** Пустое тело.

**Как использовать на фронте:**

JavaScript

```
if (confirm('Вы точно хотите удалить аккаунт? Это необратимо!')) {
  await fetch('/api/users/me', {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
    }
  });

  localStorage.clear();
  window.location.href = '/';
}
```

---

### 4.4 Посмотреть публичный профиль другого пользователя

text

```
GET /api/users/{id}
```

**Что делает:** Возвращает ПУБЛИЧНУЮ информацию о другом пользователе. Не email, не настройки — только то, что можно показывать всем (имя, аватарка и т.д.).

**Нужна авторизация:** Нет (или да — зависит от настроек security, но обычно нет)

**Что отправить:** Ничего. ID пользователя в URL.

**Что придёт в ответ (200 OK):**

JSON

```
{
  "id": 99,
  "username": "cool_user",
  "displayName": "Крутой Чувак",
  "avatarUrl": "https://example.com/avatars/99.jpg"
}
```

**Как использовать на фронте:**

JavaScript

```
const userId = 99;
const response = await fetch(`/api/users/${userId}`);
const publicProfile = await response.json();
```