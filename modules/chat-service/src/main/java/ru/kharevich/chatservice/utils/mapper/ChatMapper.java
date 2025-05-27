package ru.kharevich.chatservice.utils.mapper;

import org.mapstruct.*;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.model.Chat;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ChatMapper {

    @Mapping(target = "id", expression = "java(chat.getId().toHexString())")
    ChatResponse toResponse(Chat chat);

    Chat toEntity(ChatRequest chatRequest);

}
