package ru.kharevich.userservice.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import static ru.kharevich.userservice.util.constants.ApplicationConstants.USERNAME_CLAIM_NAME;
import static ru.kharevich.userservice.util.constants.ApplicationConstants.USER_ID_CLAIM_NAME;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.JWT_CLAIM_EXTRACT_EXCEPTION_MESSAGE;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.JWT_CONVERSION_EXCEPTION_MESSAGE;

@Component
public class JwtUtils {

    public String getPreferredUsername() {
        Authentication authentication = getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object preferredUsername = jwt.getClaims().get(USERNAME_CLAIM_NAME);

            if (preferredUsername != null) {
                return preferredUsername.toString();
            } else {
                throw new IllegalStateException(JWT_CLAIM_EXTRACT_EXCEPTION_MESSAGE.formatted(USERNAME_CLAIM_NAME));
            }
        }

        throw new IllegalStateException(JWT_CONVERSION_EXCEPTION_MESSAGE);
    }

    public String getUserId() {
        Authentication authentication = getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object preferredUsername = jwt.getClaims().get(USER_ID_CLAIM_NAME);

            if (preferredUsername != null) {
                return preferredUsername.toString();
            } else {
                throw new IllegalStateException(JWT_CLAIM_EXTRACT_EXCEPTION_MESSAGE.formatted(USER_ID_CLAIM_NAME));
            }
        }

        throw new IllegalStateException(JWT_CONVERSION_EXCEPTION_MESSAGE);
    }

    private Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
