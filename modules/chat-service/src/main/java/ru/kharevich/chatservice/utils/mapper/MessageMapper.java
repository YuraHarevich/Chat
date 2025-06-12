package ru.kharevich.chatservice.utils.mapper;

import org.bson.types.ObjectId;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.request.MessageRequestWebSocket;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.model.Message;

import java.util.UUID;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface MessageMapper {

    @Mapping(target = "id", expression = "java(message.getId().toHexString())")
    @Mapping(target = "chatId", expression = "java(message.getChatId().toHexString())")
    @Mapping(target = "sharedId", expression = "java(message.getSharedId().toString())")
    MessageResponse toResponse(Message message);

    Message toEntity(MessageRequest messageRequest, ObjectId chatId);

    Message toEntityV2(MessageRequestWebSocket messageRequest, UUID sender);

}
