package ru.kharevich.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserRequest(

        @NotNull(message = "username cannot be null")
        String username,

        @NotNull(message = "firstname cannot be null")
        String firstname,

        @NotNull(message = "lastname cannot be null")
        String lastname,

        @Email(message = "email is not valid")
        String email,

        @NotNull(message = "password cannot be null")
        String password

) {
}
