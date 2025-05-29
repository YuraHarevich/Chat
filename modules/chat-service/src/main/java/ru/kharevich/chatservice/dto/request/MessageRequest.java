package ru.kharevich.chatservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageRequest(

        @NotBlank(message = "Message content must not be blank")
        String content,

        @NotNull(message = "Sender ID must not be null")
        UUID sender,

        @NotNull(message = "Shared Chat ID must not be null")
        UUID sharedId,

        @NotNull(message = "Owner ID must not be null")
        UUID owner

) {
}
