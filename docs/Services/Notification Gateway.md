#### 5. Notification Gateway
Сервис-"подписчик", ни от кого не зависит, кроме Kafka.

- **Ответственность:**
    - Доставление уведомлений, если юзер оффлайн.
    - Агрегация уведомлений (не спамить 100 пушей за секунду).
    - Каналы доставки: Firebase (FCM), APNs (Apple), Email (SMTP), SMS (Twilio).
- **Технологии:**
    - Spring Boot + Kafka Consumer.
    - Внешние SDK (Firebase Admin SDK).
- **Интеграция:**
    - Слушает **ВСЕ** события: `MessageSent` (из Messaging), `SpaceStarted` (из Space), `NewRelease` (из Media).