package com.example.cinema.client.users;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class UserClientHttp implements UserClient {

    private final RestClient restClient;

    @Value("${services.users.url}")
    private String usersServiceUrl;

    public UserClientHttp(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public String getUserName(UUID userId) {
        try {
            UserSummaryResponse response = restClient.get()
                    .uri(usersServiceUrl + "/users/internal/{userId}", userId)
                    .retrieve()
                    .body(UserSummaryResponse.class);
            return response != null ? response.name() : null;
        } catch (RestClientException e) {
            return null;
        }
    }
}
