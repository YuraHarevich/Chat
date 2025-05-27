package ru.kharevich.chatservice.utils.mapper;

import org.bson.types.ObjectId;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.model.Message;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface MessageMapper {

    @Mapping(target = "id", expression = "java(message.getId().toHexString())")
    MessageResponse toResponse(Message message);

    Message toEntity(MessageRequest messageRequest, ObjectId chatId);

}
