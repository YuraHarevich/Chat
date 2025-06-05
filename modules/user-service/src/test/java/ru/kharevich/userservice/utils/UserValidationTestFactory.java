package ru.kharevich.userservice.utils;

import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.model.AccountStatus;
import ru.kharevich.userservice.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserValidationTestFactory {

    public static User userWithStatus(UUID id, AccountStatus status) {
        return User.builder()
                .id(id)
                .externalId(UUID.randomUUID())
                .username("testuser")
                .firstname("Test")
                .lastname("User")
                .email("test@example.com")
                .birthDate(LocalDateTime.of(2000, 1, 1, 0, 0))
                .accountStatus(status)
                .build();
    }

    public static User userWithId(UUID id) {
        return User.builder()
                .id(id)
                .username("anotherUser")
                .email("another@example.com")
                .accountStatus(AccountStatus.EXISTS)
                .build();
    }

    public static UserRequest validUserRequest() {
        return new UserRequest(
                "uniqueUsername",
                "Valid",
                "User",
                "valid@example.com",
                "securePassword123"
        );
    }
}

