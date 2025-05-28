package ru.kharevich.chatservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import ru.kharevich.chatservice.model.MessageStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageRequest(

        @NotBlank(message = "Message content must not be blank")
        String content,

        @NotNull(message = "Sender ID must not be null")
        UUID sender,

        @NotNull(message = "Receiver ID must not be null")
        UUID receiver,

        @NotNull(message = "Chat ID must not be null")
        ObjectId chatId

) {
}
