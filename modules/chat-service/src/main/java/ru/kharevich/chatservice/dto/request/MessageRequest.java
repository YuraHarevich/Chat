package ru.kharevich.chatservice.dto.request;

import org.bson.types.ObjectId;
import ru.kharevich.chatservice.model.MessageStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageRequest (

    String content,

    UUID sender,

    UUID receiver,

    ObjectId chatId

) {

}
