package ru.kharevich.chatservice.dto.response;

public record FrontChatResponse(

        String username,

        String chatId,

        String sharedId

) {
}