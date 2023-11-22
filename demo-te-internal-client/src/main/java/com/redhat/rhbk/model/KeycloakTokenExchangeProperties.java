package com.redhat.rhbk.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "keycloak.token.exchange")
public class KeycloakTokenExchangeProperties {
    private String endpoint;
    private String clientId;
    private String clientSecret;
}
