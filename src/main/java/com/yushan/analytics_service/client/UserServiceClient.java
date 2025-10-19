package com.yushan.analytics_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "${services.user.name}",
        url = "${services.user.url}"
)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}/validate")
    boolean validateUser(@PathVariable("id") UUID userId);
}