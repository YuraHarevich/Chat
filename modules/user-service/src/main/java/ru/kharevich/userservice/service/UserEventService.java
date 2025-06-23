package ru.kharevich.userservice.service;

import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;

import java.util.UUID;

public interface UserEventService {
    UserResponse createUserPostEvent(UserRequest dto);

    void deleteUserPostEvent(UUID id);

    UserResponse updateUserPostEvent(UUID id, UserRequest request);

    public UserResponse recoverTheAccountAndPostEvent(AccountRecoverRequest request);
}
