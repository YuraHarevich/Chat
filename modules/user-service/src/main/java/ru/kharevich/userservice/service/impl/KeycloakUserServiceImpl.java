package ru.kharevich.userservice.service.impl;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.exceptions.UserCreationException;
import ru.kharevich.userservice.service.KeycloakUserService;
import ru.kharevich.userservice.util.props.KeycloakProperties;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_CREATION_EXCEPTION_MESSAGE;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_CREATION_EXCEPTION_WHILE_REQUEST_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;

    @Override
    public UUID createUser(UserRequest request) {
        UserRepresentation keycloakUser = createKeycloakUser(request);
        RealmResource realmResource = keycloak.realm(keycloakProperties.getRealm());
        UsersResource usersResource = realmResource.users();

        Response response = Optional.ofNullable(usersResource.create(keycloakUser))
                .orElseThrow(() -> new UserCreationException(USER_CREATION_EXCEPTION_WHILE_REQUEST_MESSAGE));

        if (response.getStatus() == HttpStatus.CREATED.value()) {
            String keycloakUserId = CreatedResponseUtil.getCreatedId(response);
            assignRoleToUser(realmResource, usersResource, keycloakProperties.getDefaultRole(), keycloakUserId);
            return UUID.fromString(keycloakUserId);
        } else {
            throw new UserCreationException(USER_CREATION_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public void updateUser(String userId, UserRequest request) {
        RealmResource realmResource = keycloak.realm(keycloakProperties.getRealm());
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(userId);

        UserRepresentation userRepresentation = userResource.toRepresentation();
        userRepresentation.setUsername(request.username());
        userRepresentation.setFirstName(request.firstname());
        userRepresentation.setLastName(request.lastname());
        userRepresentation.setEmail(request.email());
        userRepresentation.setEnabled(true);

        // Обновление пароля, если он предоставлен
        if (request.password() != null && !request.password().isEmpty()) {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.password());
            credential.setTemporary(false);
            userResource.resetPassword(credential);
        }

        userResource.update(userRepresentation);
    }

    @Override
    public void deleteUser(String userId) {
        RealmResource realmResource = keycloak.realm(keycloakProperties.getRealm());
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(userId);

        userResource.remove();
    }

    @Override
    public UserRepresentation getUserById(String userId) {
        RealmResource realmResource = keycloak.realm(keycloakProperties.getRealm());
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(userId);
        return userResource.toRepresentation();
    }

    private UserRepresentation createKeycloakUser(UserRequest request) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);

        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(request.username());
        keycloakUser.setFirstName(request.firstname());
        keycloakUser.setLastName(request.lastname());
        keycloakUser.setEmail(request.email());
        keycloakUser.setCredentials(List.of(credential));
        keycloakUser.setEnabled(true);

        return keycloakUser;
    }

    private void assignRoleToUser(RealmResource realmResource, UsersResource usersResource, String role, String userId) {
        RolesResource rolesResource = realmResource.roles();
        RoleRepresentation roleRepresentation = rolesResource.get(role).toRepresentation();
        UserResource userResource = usersResource.get(userId);
        userResource.roles().realmLevel().add(List.of(roleRepresentation));
    }
}
