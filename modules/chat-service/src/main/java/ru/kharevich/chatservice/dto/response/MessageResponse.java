package ru.kharevich.chatservice.dto.response;

import org.bson.types.ObjectId;

import java.util.UUID;

public record MessageResponse(

        String id,

        String content,

        UUID sender,

        UUID receiver,

        ObjectId chatId

) {

}