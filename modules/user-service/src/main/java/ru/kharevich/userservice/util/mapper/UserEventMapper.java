package ru.kharevich.userservice.util.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.kharevich.userservice.dto.events.UserEventTransferEntity;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;
import ru.kharevich.userservice.model.User;
import ru.kharevich.userservice.model.UserModifyEventType;

import java.util.UUID;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface UserEventMapper {

    UserEventTransferEntity toEventEntity(User user, UserModifyEventType eventType, String password);

    UserRequest toUserRequest(UserEventTransferEntity eventType);

}
