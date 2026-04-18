package com.example.feedservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Value("${app.gateway.url:http://localhost:8000}")
    private String gatewayUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Feed Service API")
                        .version("1.0.0")
                        .description("""
                                ## Сервис ленты новостей
                                
                                ### Быстрый старт:
                                1. Получи токен: `POST /api/auth/login` в Identity Service
                                2. Нажми **Authorize** вверху справа 🔓
                                3. Вставь токен (без "Bearer ")
                                4. Делай запросы!
                                
                                ### Тестовые данные:
                                - **postId**: `1`, `2`, `3`
                                - **userId**: `1`, `2`
                                - **groupId**: `1`
                                - **commentId**: `1`, `2`
                                
                                ### Пагинация:
                                Все списки возвращают объект:
                                ```json
                                {
                                  "content": [...],
                                  "page": 0,
                                  "size": 20,
                                  "totalElements": 100,
                                  "totalPages": 5,
                                  "last": false
                                }
                                ```
                                
                                ### Частые ошибки:
                                | Код | Что делать |
                                |-----|------------|
                                | 401 | Токен истёк → перелогинься |
                                | 403 | Нет прав (чужой пост и т.д.) |
                                | 404 | Неверный ID |
                                | 409 | Уже сделано (лайк/подписка) |
                                """))
                .servers(List.of(
                        new Server().url(gatewayUrl).description("Gateway")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                **Как получить:**
                                                ```
                                                POST /api/auth/login
                                                {"email": "test@test.com", "password": "123456"}
                                                ```
                                                Скопируй `accessToken` из ответа и вставь сюда.
                                                """)))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
