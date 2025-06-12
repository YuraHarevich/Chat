package ru.kharevich.chatservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;

import java.util.UUID;

public record MessageRequestWebSocket(

        @NotBlank(message = "Message content must not be blank")
        String content,

        @NotNull(message = "Chat ID must not be null")
        ObjectId chatId,

        UUID sharedId

) {
}
