package com.example.feedservice.client;

import com.example.feedservice.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Component
public class MessagingServiceClient {

    private final RestClient restClient;

    public MessagingServiceClient(AppProperties appProperties) {
        this.restClient = RestClient.builder()
                .baseUrl(appProperties.services().messagingUrl())
                .build();
    }

    public List<Long> checkPostPermissions(Long userId, List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return List.of();
        }

        try {
            var response = restClient.post()
                    .uri("/internal/groups/check-post-permissions")
                    .body(new CheckPermissionsRequest(userId, groupIds))
                    .retrieve()
                    .body(CheckPermissionsResponse.class);

            return response != null ? response.allowedGroupIds() : List.of();

        } catch (RestClientException e) {
            log.error("Failed to check group permissions for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    public List<Long> getUserGroupIds(Long userId) {
        try {
            var response = restClient.get()
                    .uri("/internal/users/{userId}/groups", userId)
                    .retrieve()
                    .body(UserGroupsResponse.class);

            return response != null ? response.groupIds() : List.of();

        } catch (RestClientException e) {
            log.error("Failed to get groups for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    public record CheckPermissionsRequest(Long userId, List<Long> groupIds) {}

    public record CheckPermissionsResponse(List<Long> allowedGroupIds) {}

    public record UserGroupsResponse(List<Long> groupIds) {}
}