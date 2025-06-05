package ru.kharevich.userservice.service;

import ru.kharevich.userservice.dto.events.UserEventTransferEntity;

public interface SagaEventHandlerService {
    public void handleEvent(UserEventTransferEntity message);
}
