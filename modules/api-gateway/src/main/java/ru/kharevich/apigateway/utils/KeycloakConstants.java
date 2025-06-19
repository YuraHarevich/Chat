package ru.kharevich.apigateway.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeycloakConstants {

    public static final String REALM_ACCESS_CLAIM = "realm_access";
    public static final String ROLE_CLAIM = "roles";
    public static final String ROLE_PREFIX = "ROLE_";

}