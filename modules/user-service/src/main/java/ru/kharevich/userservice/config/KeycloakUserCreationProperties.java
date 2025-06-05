package ru.kharevich.userservice.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeycloakUserCreationProperties {

    public static final boolean USER_ENABLED_STATUS = true;

    public static final boolean USER_EMAIL_VERIFIED_STATUS = false;

    public static final boolean CREDENTIALS_ARE_TEMPORARY = false;

    public static final String CREDENTIALS_REPRESENTATION_TYPE = CredentialRepresentation.PASSWORD;

}
