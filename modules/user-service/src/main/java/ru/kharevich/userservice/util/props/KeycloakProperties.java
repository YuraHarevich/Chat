package ru.kharevich.userservice.util.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak")
@Getter
@Setter
public class KeycloakProperties {

    private String clientId;

    private String domain;

    private String clientSecret;

    private String authUrl;

    private String realm;

}
