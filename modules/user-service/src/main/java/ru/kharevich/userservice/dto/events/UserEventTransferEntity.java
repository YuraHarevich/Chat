package ru.kharevich.userservice.dto.events;

import ru.kharevich.userservice.model.UserModifyEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserEventTransferEntity(

        UUID id,

        UUID externalId,

        String username,

        String firstname,

        String lastname,

        LocalDateTime birthDate,

        String email,

        String password,

        UserModifyEventType eventType

) {
}
