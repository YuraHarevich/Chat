package ru.kharevich.userservice.service;

import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import ru.kharevich.userservice.dto.request.SignInRequest;
import ru.kharevich.userservice.dto.request.UserRequest;

import java.util.UUID;

public interface KeycloakUserService {

    public UUID createKeycloakUser(UserRequest userRequest, UUID id);

    AccessTokenResponse sighIn(SignInRequest request);

    void updateUser(String userId, UserRequest request);

    void deleteUser(String userId);

}
