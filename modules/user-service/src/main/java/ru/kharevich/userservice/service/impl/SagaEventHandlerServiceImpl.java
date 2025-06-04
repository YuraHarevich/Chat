package ru.kharevich.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kharevich.userservice.dto.events.UserEventTransferEntity;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.service.KeycloakUserService;
import ru.kharevich.userservice.service.SagaEventHandlerService;
import ru.kharevich.userservice.service.UserService;
import ru.kharevich.userservice.util.mapper.UserEventMapper;
import ru.kharevich.userservice.util.mapper.UserMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaEventHandlerServiceImpl implements SagaEventHandlerService {

    public final KeycloakUserService keycloakUserService;

    public final UserService userService;

    private final UserEventMapper userEventMapper;

    @Override
    public void handleEvent(UserEventTransferEntity message) {
        UserRequest userRequest = userEventMapper.toUserRequest(message);

        switch(message.eventType()){
            case CREATE_EVENT -> {
                UUID externalId = keycloakUserService.createUser(userRequest);
                userService.setExternalId(externalId, message.id());
                userService.setExistsStatus(message.id());
            }
            case UPDATE_EVENT -> {
                keycloakUserService.updateUser(message.externalId().toString(), userRequest);
                userService.setExistsStatus(message.id());
            }
            case DELETE_EVENT -> {
                keycloakUserService.deleteUser(message.externalId().toString());
            }
            case CREATE_COMPENSATION_EVENT -> {
                userService.delete(message.id());
            }
            case UPDATE_COMPENSATION_EVENT -> {
                userService.update(message.id(), userRequest);
            }
            case DELETE_COMPENSATION_EVENT -> {
                userService.recoverTheAccount(
                        new AccountRecoverRequest(message.id(), message.password()));
            }
        }

    }

}
