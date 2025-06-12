package ru.kharevich.chatservice.utils.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.FrontChatResponse;
import ru.kharevich.chatservice.model.Chat;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ChatMapper {

    @Mapping(target = "id", expression = "java(chat.getId().toHexString())")
    ChatResponse toResponse(Chat chat);

    Chat toEntity(ChatRequest chatRequest);

    @Mapping(target = "chatId", expression = "java(chat.getId().toHexString())")
    @Mapping(target = "sharedId", expression = "java(chat.getSharedId().toString())")
    FrontChatResponse toFrontChatResponse(Chat chat, String username);

}