package ru.kharevich.chatservice.dto.response;

import java.io.Serializable;

public record FrontChatResponse(

        String username,

        String chatId,

        String sharedId

) implements Serializable {
}