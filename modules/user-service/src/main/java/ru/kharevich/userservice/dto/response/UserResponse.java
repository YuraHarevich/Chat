package ru.kharevich.userservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(

        UUID id,

        UUID externalId,

        String username,

        String firstname,

        String lastname,

        LocalDateTime birthDate,

        String email
) {
}
