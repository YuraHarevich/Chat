package ru.kharevich.userservice.service;

import org.keycloak.representations.idm.UserRepresentation;
import ru.kharevich.userservice.dto.request.UserRequest;

import java.util.UUID;

public interface KeycloakUserService {
    public UUID createUser(UserRequest userRequest);

    void updateUser(String userId, UserRequest request);

    void deleteUser(String userId);

    UserRepresentation getUserById(String userId);
}
