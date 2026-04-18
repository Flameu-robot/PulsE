package com.example.identityservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.gateway.url:http://localhost:8000}")
    private String gatewayUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Identity Service API")
                        .version("1.0")
                        .description("""
                                ## Authentication, authorization and user management
                                
                                ### Быстрый старт:
                                1. Зарегистрируйся: `POST /api/auth/register`
                                2. Или войди: `POST /api/auth/login`
                                3. Скопируй `accessToken` из ответа
                                4. Нажми **Authorize** 🔓 и вставь токен
                                
                                ### Тестовые данные:
                                - **email**: `test@example.com`
                                - **password**: `Password123!`
                                - **userId**: `1`, `2`
                                
                                ### Время жизни токенов:
                                - **accessToken**: 15 минут
                                - **refreshToken**: 30 дней
                                
                                ### Эндпоинты БЕЗ авторизации:
                                - `POST /api/auth/register` — регистрация
                                - `POST /api/auth/login` — вход
                                - `POST /api/auth/refresh` — обновление токена
                                - `POST /api/auth/password/forgot` — забыл пароль
                                - `POST /api/auth/password/reset` — сброс пароля
                                - `GET /api/users/{id}` — публичный профиль
                                - `GET /api/users/by-username/{username}` — публичный профиль по username
                                - `POST /api/auth/webauthn/login/*` — вход по ключу
                                
                                ### Частые ошибки:
                                | Код | Причина |
                                |-----|---------|
                                | 401 | Токен истёк или невалидный |
                                | 403 | Нет прав |
                                | 404 | Пользователь не найден |
                                | 409 | Email уже занят |
                                | 422 | Неверный код подтверждения |
                                """))
                .servers(List.of(
                        new Server()
                                .url(gatewayUrl)
                                .description("Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Вставь accessToken без префикса 'Bearer '")
                        )
                );
    }
}