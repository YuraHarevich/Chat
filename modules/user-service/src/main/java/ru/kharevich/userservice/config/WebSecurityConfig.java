package ru.kharevich.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.kharevich.userservice.util.converter.JwtAuthConverter;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    @Profile("!test")
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        //.requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/sign-in").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/sign-up").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/**").permitAll()
                        .anyRequest().hasRole("USER"))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthConverter)
                        )
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(CsrfConfigurer::disable)
                .build();
    }

    @Bean
    @Profile("test")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .anyRequest().permitAll())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(CsrfConfigurer::disable)
                .build();
    }

}
