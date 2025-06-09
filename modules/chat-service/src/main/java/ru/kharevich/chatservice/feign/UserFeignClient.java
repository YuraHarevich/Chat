package ru.kharevich.chatservice.feign;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.kharevich.chatservice.external.reponse.UserResponse;

import java.util.UUID;

public interface UserFeignClient {

    @GetMapping("/api/v1/users/{id}")
    UserResponse getUserIfExists(@PathVariable("id") UUID id);

}
