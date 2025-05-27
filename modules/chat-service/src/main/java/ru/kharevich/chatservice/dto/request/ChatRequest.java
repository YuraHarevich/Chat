package ru.kharevich.chatservice.dto.request;

import java.util.Set;
import java.util.UUID;

public record ChatRequest (
        Set<UUID> participants
) {
}
