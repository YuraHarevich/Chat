package ru.kharevich.userservice.service.impl;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.exceptions.RepeatedUserDataException;
import ru.kharevich.userservice.exceptions.UserCreationException;
import ru.kharevich.userservice.service.KeycloakUserService;
import ru.kharevich.userservice.util.props.KeycloakProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ru.kharevich.userservice.config.KeycloakUserCreationProperties.CREDENTIALS_ARE_TEMPORARY;
import static ru.kharevich.userservice.config.KeycloakUserCreationProperties.CREDENTIALS_REPRESENTATION_TYPE;
import static ru.kharevich.userservice.config.KeycloakUserCreationProperties.USER_EMAIL_VERIFIED_STATUS;
import static ru.kharevich.userservice.config.KeycloakUserCreationProperties.USER_ENABLED_STATUS;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_CREATION_EXCEPTION;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_REPEATED_DATA_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private final Keycloak keycloak;

    private final KeycloakProperties keycloakProperties;

    @SuppressWarnings("checkstyle:FallThrough")
    @Override
    public UUID createUser(UserRequest request) {

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(USER_ENABLED_STATUS);
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstname());
        user.setLastName(request.lastname());
        user.setEmailVerified(USER_EMAIL_VERIFIED_STATUS);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(request.password());
        credentialRepresentation.setTemporary(CREDENTIALS_ARE_TEMPORARY);
        credentialRepresentation.setType(CREDENTIALS_REPRESENTATION_TYPE);


        List<CredentialRepresentation> list = new ArrayList<>();
        list.add(credentialRepresentation);
        user.setCredentials(list);

        UsersResource usersResource = getUsersResource();

        Response response = usersResource.create(user);
        int responseStatus = response.getStatus();
        log.info("UserService.Keycloak create user response with status {}", responseStatus);

        switch (responseStatus) {
            case 201:
                List<UserRepresentation> representationList = usersResource.searchByUsername(request.username(), true);
                if (!CollectionUtils.isEmpty(representationList)) {
                    UserRepresentation userRepresentationForCreatedUser = representationList
                            .stream()
                            .filter(userRepresentation -> Objects.equals(false, userRepresentation.isEmailVerified()))
                            .findFirst()
                            .orElse(null);
                    assert userRepresentationForCreatedUser != null;
                    String userId = userRepresentationForCreatedUser.getId();

                    log.info("UserService.Senging verification email from user with id {}", userId);
                    emailVerification(userId);

                    try {
                        assignRole(userId, "USER");
                    } catch (ForbiddenException exception) {
                        log.error("UserService.Client don't have enough rights to assign role: {}", exception.getMessage());
//                        deleteUserById(UUID.fromString(userId));
//                        throw new ClientRightException(USER_CREATION_ERROR);
                    }

                    return UUID.fromString(userId);
                }
            case 409:
                throw new RepeatedUserDataException(USER_REPEATED_DATA_MESSAGE);
            default:
                throw new UserCreationException(USER_CREATION_EXCEPTION);
        }
    }


    private UserResource getUserResource(String userId) {
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }

    private UsersResource getUsersResource() {
        RealmResource realm = keycloak.realm(keycloakProperties.getRealm());
        return realm.users();
    }

    private void emailVerification(String userId) {
        log.info("sending email verification");
        UsersResource usersResource = getUsersResource();
        usersResource.get(userId).sendVerifyEmail();
    }

    private void assignRole(String userId, String roleName) {
        UserResource userResource = getUserResource(userId);
        RolesResource rolesResource = getRolesResource();
        RoleRepresentation representation = rolesResource.get(roleName).toRepresentation();
        userResource.roles().realmLevel().add(Collections.singletonList(representation));
    }

    private RolesResource getRolesResource() {
        return keycloak.realm(keycloakProperties.getRealm()).roles();
    }

}
