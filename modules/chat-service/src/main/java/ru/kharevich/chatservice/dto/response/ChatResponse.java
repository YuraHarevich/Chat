package ru.kharevich.chatservice.dto.response;

import java.util.Set;
import java.util.UUID;

public record ChatResponse(

        String id,

        UUID sharedId,

        Set<UUID> participants

) {
}
