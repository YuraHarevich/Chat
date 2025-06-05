package ru.kharevich.userservice.dto.request;

import jakarta.validation.constraints.NotNull;

public record SignInRequest(

        @NotNull(message = "username cannot be null")
        String username,

        @NotNull(message = "password cannot be null")
        String password
) {
}
