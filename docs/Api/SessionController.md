### 3.1 Получить список активных сессий

text

```
GET /api/auth/sessions
```

**Что делает:** Показывает все устройства/браузеры, где пользователь сейчас залогинен. Например: «Chrome на Windows», «Safari на iPhone» и т.д.

**Нужна авторизация:** ДА

**Что отправить:** Ничего.

**Что придёт в ответ (200 OK):**

JSON

```
[
  {
    "id": 1,
    "deviceInfo": "Chrome 120, Windows 10",
    "ipAddress": "192.168.1.1",
    "createdAt": "2024-01-15T10:30:00",
    "lastUsedAt": "2024-01-16T14:22:00"
  },
  {
    "id": 2,
    "deviceInfo": "Safari, iPhone",
    "ipAddress": "10.0.0.5",
    "createdAt": "2024-01-14T08:15:00",
    "lastUsedAt": "2024-01-16T09:00:00"
  }
]
```

**Как использовать на фронте:**

JavaScript

```
const response = await fetch('/api/auth/sessions', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

const sessions = await response.json();
sessions.forEach(s => {
  console.log(`Сессия #${s.id}: ${s.deviceInfo} (IP: ${s.ipAddress})`);
});
```

---

### 3.2 Завершить конкретную сессию

text

```
DELETE /api/auth/sessions/{id}
```

**Что делает:** Убивает одну конкретную сессию по её ID. Например, увидел в списке подозрительную сессию — нажал «завершить».

**Нужна авторизация:** ДА

**Что отправить:** Ничего, ID сессии в URL.

**Что придёт в ответ (204 No Content):** Пустое тело.

**Как использовать на фронте:**

JavaScript

```
const sessionId = 2; // ID сессии, которую хотим убить

await fetch(`/api/auth/sessions/${sessionId}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

alert('Сессия завершена!');
// Обновляем список сессий на странице
```