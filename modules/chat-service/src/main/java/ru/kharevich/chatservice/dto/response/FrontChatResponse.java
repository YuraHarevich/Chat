package ru.kharevich.chatservice.dto.response;

import org.bson.types.ObjectId;

import java.util.Set;
import java.util.UUID;

public record FrontChatResponse(

        String username,

        String chatId,

        String sharedId

) {
}