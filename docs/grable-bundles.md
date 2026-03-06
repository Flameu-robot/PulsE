|                   |                                                 |                          |
| ----------------- | ----------------------------------------------- | ------------------------ |
| `spring-base`     | starter, web, validation, actuator              | Все микросервисы         |
| `spring-security` | security, oauth2-resource-server, oauth2-client | Identity Service         |
| `jwt`             | jjwt-api, impl, jackson                         | Identity Service         |
| `webauthn`        | webauthn-server-core, attestation               | Identity Service         |
| `database`        | flyway-core, flyway-postgresql                  | Все сервисы с PostgreSQL |
| `testcontainers`  | junit, postgresql, kafka                        | Интеграционные тесты     |
| `testing`         | starter-test, security-test, kafka-test         | Все микросервисы         |