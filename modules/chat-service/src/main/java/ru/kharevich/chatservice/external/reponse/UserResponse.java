package ru.kharevich.chatservice.external.reponse;

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
