package ru.kharevich.userservice.util.converter;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.kharevich.userservice.exceptions.JwtConverterException;

import java.util.Collection;
import java.util.stream.Collectors;

import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.JWT_CONVERT_EXCEPTION_MESSAGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities;
        AccessToken token = null;
        try {
            token = TokenVerifier.create(jwt.getTokenValue(), AccessToken.class).getToken();
        } catch (VerificationException e) {
            log.error("JwtAuthConverter.convert: " + e.getMessage());
            throw new JwtConverterException(JWT_CONVERT_EXCEPTION_MESSAGE);
        }

        authorities = token.getRealmAccess().getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaim(JwtClaimNames.SUB));
    }

}
