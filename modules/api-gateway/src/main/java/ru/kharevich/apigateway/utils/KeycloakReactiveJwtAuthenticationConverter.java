package ru.kharevich.apigateway.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.kharevich.apigateway.utils.KeycloakConstants.REALM_ACCESS_CLAIM;
import static ru.kharevich.apigateway.utils.KeycloakConstants.ROLE_CLAIM;
import static ru.kharevich.apigateway.utils.KeycloakConstants.ROLE_PREFIX;

@Slf4j
public class KeycloakReactiveJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        log.info("KeycloakReactiveJwtAuthenticationConverter.convert");
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        log.info("KeycloakReactiveJwtAuthenticationConverter.convert: Jwt successfully converted to JwtAuthenticationToken with authorities {}", authorities);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess != null && realmAccess.get(ROLE_CLAIM) instanceof Collection<?>) {
            authorities = ((Collection<?>) realmAccess.get(ROLE_CLAIM))
                    .stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(role -> new SimpleGrantedAuthority(
                            ROLE_PREFIX
                                    + role.replace(ROLE_PREFIX, StringUtils.EMPTY)))
                    .collect(Collectors.toList());
        }
        log.debug("KeycloakReactiveJwtAuthenticationConverter.extractAuthorities: Authorities extracted from JWT roles {}", authorities);
        return authorities;
    }

}

