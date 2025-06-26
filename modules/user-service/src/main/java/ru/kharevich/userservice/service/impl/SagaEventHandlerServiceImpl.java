package ru.kharevich.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kharevich.userservice.dto.events.UserEventTransferEntity;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.kafka.producer.UserEventProducer;
import ru.kharevich.userservice.service.KeycloakUserService;
import ru.kharevich.userservice.service.SagaEventHandlerService;
import ru.kharevich.userservice.service.UserService;
import ru.kharevich.userservice.util.mapper.UserEventMapper;

import java.util.UUID;

import static ru.kharevich.userservice.model.UserModifyEventType.CREATE_COMPENSATION_EVENT;
import static ru.kharevich.userservice.model.UserModifyEventType.DELETE_COMPENSATION_EVENT;
import static ru.kharevich.userservice.model.UserModifyEventType.UPDATE_COMPENSATION_EVENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaEventHandlerServiceImpl implements SagaEventHandlerService {

    public final KeycloakUserService keycloakUserService;

    public final UserService userService;

    private final UserEventMapper userEventMapper;

    private final UserEventProducer userEventProducer;

    @Override
    public void handleEvent(UserEventTransferEntity message) {
        UserRequest userRequest = userEventMapper.toUserRequest(message);

        log.info("SagaEventHandlerServiceImpl.handleEvent: handling {} event", message.eventType());
        switch (message.eventType()) {
            case CREATE_EVENT -> {
                UUID externalId = null;
                try {
                    externalId = keycloakUserService.createKeycloakUser(userRequest, message.id());
                } catch (Exception ex) {
                    log.debug(ex.getMessage());
                    UserEventTransferEntity newMessage = new UserEventTransferEntity(
                            message.id(),
                            message.externalId(),
                            message.username(),
                            message.firstname(),
                            message.lastname(),
                            message.birthDate(),
                            message.email(),
                            message.password(),
                            CREATE_COMPENSATION_EVENT);
                    userEventProducer.publishEventRequest(newMessage);
                }
                userService.setExternalId(externalId, message.id());
                userService.setExistsStatus(message.id());
            }
            case UPDATE_EVENT -> {
                try {
                    keycloakUserService.updateUser(message.externalId().toString(), userRequest);
                } catch (Exception ex) {
                    log.debug(ex.getMessage());
                    UserEventTransferEntity newMessage = new UserEventTransferEntity(
                            message.id(),
                            message.externalId(),
                            message.username(),
                            message.firstname(),
                            message.lastname(),
                            message.birthDate(),
                            message.email(),
                            message.password(),
                            UPDATE_COMPENSATION_EVENT);
                    userEventProducer.publishEventRequest(newMessage);
                }
                userService.setExistsStatus(message.id());

            }
            case DELETE_EVENT -> {
                try {
                    keycloakUserService.deleteUser(message.externalId().toString());
                } catch (Exception ex) {
                    log.debug(ex.getMessage());
                    UserEventTransferEntity newMessage = new UserEventTransferEntity(
                            message.id(),
                            message.externalId(),
                            message.username(),
                            message.firstname(),
                            message.lastname(),
                            message.birthDate(),
                            message.email(),
                            message.password(),
                            DELETE_COMPENSATION_EVENT);
                    userEventProducer.publishEventRequest(newMessage);
                }
            }
            case CREATE_COMPENSATION_EVENT -> {
                userService.absoluteDelete(message.id());
            }
            case UPDATE_COMPENSATION_EVENT -> {
                userService.update(userRequest);
                userService.setExistsStatus(message.id());
            }
            case DELETE_COMPENSATION_EVENT -> {
                userService.recoverTheAccount(
                        new AccountRecoverRequest(message.id(), message.password()));
                userService.setExistsStatus(message.id());
            }
        }

    }

}
