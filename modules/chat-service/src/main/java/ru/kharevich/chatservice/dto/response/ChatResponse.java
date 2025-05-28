package ru.kharevich.chatservice.dto.response;

import java.util.Set;
import java.util.UUID;

public record ChatResponse(

        String id,

        Set<UUID> participants

) {
}
