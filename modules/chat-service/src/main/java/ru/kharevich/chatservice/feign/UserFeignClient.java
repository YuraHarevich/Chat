package ru.kharevich.chatservice.feign;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kharevich.chatservice.external.reponse.UserResponse;

import java.util.UUID;

public interface UserFeignClient {

    @GetMapping("/api/v1/users/id/{id}")
    UserResponse getUserIfExists(@PathVariable("id") UUID id);

    @GetMapping("/api/v1/users/username/{username}")
    UserResponse getUserByUsernameIfExists(@PathVariable("username") String username);

}
