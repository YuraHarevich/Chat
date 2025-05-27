package ru.kharevich.chatservice.dto.response;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record ChatResponse(

        String id,

        Set<UUID> participants

) {
}
