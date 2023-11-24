package com.redhat.rhbk.service;

import java.security.interfaces.RSAPublicKey;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    @Value("${spring.security.oauth2.client.public-key.location}")
    private RSAPublicKey publicKey;

    private JwtDecoder jwtDecoder;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @PostConstruct
    public void config() {
        jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    public String getAccessToken() {
        OAuth2AuthorizedClient client = getClient();
        return client.getAccessToken().getTokenValue();
    }

    public Jwt getJWT() {
        String accessToken = getAccessToken();
        if (Strings.isEmpty(accessToken)) throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        return jwtDecoder.decode(accessToken);
    }

    private OAuth2AuthorizedClient getClient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());

        if (client == null) throw new OAuth2AuthenticationException("Invalid state of session");
        return client;
    }
}
