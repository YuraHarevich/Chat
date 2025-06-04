package ru.kharevich.userservice.service;

import ru.kharevich.userservice.dto.request.UserRequest;

import java.util.UUID;

public interface KeycloakUserService {
    public UUID createUser(UserRequest userRequest);
}
