package com.example.feedservice.client;

import com.example.feedservice.config.AppProperties;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "messaging-service",
        url = "${app.services.messaging-url}",
        configuration = MessagingServiceClient.FeignConfig.class,
        fallback = MessagingServiceClient.Fallback.class
)
public interface MessagingServiceClient {

    @PostMapping("/internal/groups/check-post-permissions")
    CheckPermissionsResponse checkPostPermissions(@RequestBody CheckPermissionsRequest request);

    @GetMapping("/internal/users/{userId}/groups")
    UserGroupsResponse getUserGroupIds(@PathVariable("userId") Long userId);

    record CheckPermissionsRequest(Long userId, List<Long> groupIds) {}
    record CheckPermissionsResponse(List<Long> allowedGroupIds) {}
    record UserGroupsResponse(List<Long> groupIds) {}

    class FeignConfig {

        @Bean
        public RequestInterceptor internalTokenInterceptor(AppProperties appProperties) {
            return requestTemplate -> requestTemplate.header(
                    appProperties.internalSecurity().headerName(),
                    appProperties.internalSecurity().token()
            );
        }

        @Bean
        public ErrorDecoder errorDecoder() {
            return (methodKey, response) -> new RuntimeException(
                    "messaging-service error on " + methodKey + ": status " + response.status()
            );
        }
    }

    @Slf4j
    class Fallback implements MessagingServiceClient {

        @Override
        public CheckPermissionsResponse checkPostPermissions(CheckPermissionsRequest request) {
            log.error("Fallback: checkPostPermissions failed for user {}", request.userId());
            return new CheckPermissionsResponse(List.of());
        }

        @Override
        public UserGroupsResponse getUserGroupIds(Long userId) {
            log.error("Fallback: getUserGroupIds failed for user {}", userId);
            return new UserGroupsResponse(List.of());
        }
    }
}