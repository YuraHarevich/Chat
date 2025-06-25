package ru.kharevich.userservice.utils;

import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.model.AccountStatus;
import ru.kharevich.userservice.model.User;

import java.util.UUID;

import static ru.kharevich.userservice.model.AccountStatus.EXISTS;

public class UserTestFactory {

    public static User createValidUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .firstname("Test")
                .lastname("User")
                .email("test@example.com")
                .accountStatus(EXISTS)
                .build();
    }

    public static User createUserWithStatus(AccountStatus status) {
        return User.builder()
                .id(UUID.randomUUID())
                .username("statususer")
                .firstname("Status")
                .lastname("User")
                .email("status@example.com")
                .accountStatus(status)
                .build();
    }

    public static UserRequest createValidUserRequest() {
        return new UserRequest(
                "newuser",
                "New",
                "User",
                "new@example.com",
                "password123"
        );
    }

    public static UserRequest createInvalidUserRequest() {
        return new UserRequest(
                null,  // invalid username
                null,  // invalid firstname
                null,  // invalid lastname
                "invalid-email",  // invalid email
                null   // invalid password
        );
    }

    public static UserRequest createDuplicateUserRequest() {
        return new UserRequest(
                "existinguser",  // duplicate username
                "Existing",
                "User",
                "existing@example.com",  // duplicate email
                "password123"
        );
    }
}
