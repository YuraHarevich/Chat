package ru.kharevich.userservice.util.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.kharevich.userservice.dto.events.UserEventTransferEntity;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.model.User;
import ru.kharevich.userservice.model.UserModifyEventType;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface UserEventMapper {

    UserEventTransferEntity toEventEntity(User user, UserModifyEventType eventType, String password);

    UserRequest toUserRequest(UserEventTransferEntity eventType);

}
