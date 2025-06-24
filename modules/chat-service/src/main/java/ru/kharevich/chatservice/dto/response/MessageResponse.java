package ru.kharevich.chatservice.dto.response;

import java.io.Serializable;
import java.util.UUID;

public record MessageResponse(

        String id,

        String content,

        UUID sender,

        String chatId,

        String sharedId

) implements Serializable  {

}