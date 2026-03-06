### 2.1 Отправить код подтверждения email

text

```
POST /api/auth/verify/send
```

**Что делает:** Отправляет на почту пользователя код (например, 6 цифр). Нужно, чтобы подтвердить, что email реально принадлежит пользователю.

**Нужна авторизация:** ДА

**Что отправить:** Ничего.

**Что придёт в ответ (200 OK):**

JSON

```
{
  "message": "Verification code sent"
}
```

**Как использовать на фронте:**

JavaScript

```
await fetch('/api/auth/verify/send', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});

alert('Проверь почту — туда пришёл код!');
```

---

### 2.2 Подтвердить email кодом

text

```
POST /api/auth/verify/confirm
```

**Что делает:** Пользователь вводит код, который пришёл на почту. Если код правильный — email помечается как подтверждённый.

**Нужна авторизация:** ДА

**Что отправить:**

JSON

```
{
  "code": "483291"
}
```

**Что придёт в ответ (200 OK):**

JSON

```
{
  "message": "Email verified successfully"
}
```

**Как использовать на фронте:**

JavaScript

```
const response = await fetch('/api/auth/verify/confirm', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  },
  body: JSON.stringify({ code: '483291' })
});

const data = await response.json();
alert(data.message); // "Email verified successfully"
```

---

### 2.3 Забыл пароль (запрос кода сброса)

text

```
POST /api/auth/password/forgot
```

**Что делает:** Пользователь вводит свой email. Если такой email есть в системе — на него приходит код для сброса пароля. Ответ ВСЕГДА одинаковый (даже если email не найден) — чтобы злоумышленник не мог проверить, зарегистрирован ли email.

**Нужна авторизация:** Нет (пользователь же не может войти — он забыл пароль)

**Что отправить:**

JSON

```
{
  "email": "vasya@mail.ru"
}
```

**Что придёт в ответ (200 OK):**

JSON

```
{
  "message": "If the email exists, a reset code has been sent"
}
```

**Как использовать на фронте:**

JavaScript

```
await fetch('/api/auth/password/forgot', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'vasya@mail.ru' })
});

alert('Если аккаунт с таким email существует, мы отправили код на почту.');
// Показываем форму для ввода кода и нового пароля
```

---

### 2.4 Сброс пароля (ввод кода + новый пароль)

text

```
POST /api/auth/password/reset
```

**Что делает:** Пользователь вводит код, который пришёл на почту, и новый пароль. Если код верный — пароль меняется.

**Нужна авторизация:** Нет

**Что отправить:**

JSON

```
{
  "email": "vasya@mail.ru",
  "code": "582917",
  "newPassword": "MyNewPassword789!"
}
```

**Что придёт в ответ (200 OK):**

JSON

```
{
  "message": "Password reset successfully"
}
```

**Как использовать на фронте:**

JavaScript

```
const response = await fetch('/api/auth/password/reset', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'vasya@mail.ru',
    code: '582917',
    newPassword: 'MyNewPassword789!'
  })
});

const data = await response.json();
alert(data.message);
// Редирект на страницу логина
window.location.href = '/login';
```