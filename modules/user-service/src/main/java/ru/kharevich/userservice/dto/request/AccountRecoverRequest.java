package ru.kharevich.userservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AccountRecoverRequest(
        @NotNull(message = "id cannot be null")
        UUID id,
        @NotNull(message = "password cannot be null")
        String password
) {
}
