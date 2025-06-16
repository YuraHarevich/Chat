package ru.kharevich.apigateway;

import jakarta.ws.rs.HttpMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges

                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/sign-in").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/sign-up").permitAll()

                        .pathMatchers("/api/v1/users/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .pathMatchers("/api/v1/chats/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        .anyExchange().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                        )
                );

        return http.build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter grantedAuthoritiesExtractor() {
        return new ReactiveJwtAuthenticationConverterAdapter(new KeycloakReactiveJwtAuthenticationConverter());
    }

//    @Bean
//    public ReactiveJwtDecoder jwtDecoder() {
//        return NimbusReactiveJwtDecoder.withJwkSetUri("http://localhost:8080/realms/chat-realm/protocol/openid-connect/certs")
//                .build();
//    }
}
