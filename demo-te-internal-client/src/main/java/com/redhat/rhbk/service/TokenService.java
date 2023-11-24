package com.redhat.rhbk.service;

import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.rhbk.conf.TokenCacheConfiguration;
import com.redhat.rhbk.model.KeycloakTokenExchangeProperties;
import com.redhat.rhbk.model.KeycloakTokenExchangeResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class TokenService {
    private final EmbeddedCacheManager cacheManager;
    private final KeycloakTokenExchangeProperties keycloakTokenExchangeProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    @Value("${spring.security.oauth2.resourceserver.jwt.use-cache}")
    private Boolean useCache;
    @Value("${spring.security.oauth2.public-key.location}")
    private RSAPublicKey publicKey;
    private JwtDecoder jwtDecoder;

    @Autowired
    public TokenService(EmbeddedCacheManager cacheManager, KeycloakTokenExchangeProperties keycloakTokenExchangeProperties, RestTemplate restTemplate) {
        this.cacheManager = cacheManager;
        this.keycloakTokenExchangeProperties = keycloakTokenExchangeProperties;
        this.restTemplate = restTemplate;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @PostConstruct
    public void config(){
        jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    public Jwt getJwtFromAuthorizationHeader(String authorizationHeader) {
        if (Strings.isEmpty(authorizationHeader) || !authorizationHeader.startsWith("Bearer "))
            throw new RuntimeException("Invalid authorization header");
        final String token = authorizationHeader.split(" ")[1].trim();
        return jwtDecoder.decode(token);
    }

    public Jwt getContextJwt(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken oauthToken = (JwtAuthenticationToken) authentication;
        return oauthToken.getToken();
    }

    public Jwt getJwtFromAuthorizationHeader(String token, JwtDecoder delegate) throws JsonProcessingException {
        if (useCache) {
            Optional<Jwt> optionalJwt = getJwtFromCache(token);
            if (optionalJwt.isPresent()) {
                log.info("Returning Jwt from cache");
                return optionalJwt.get();
            }
        }

        KeycloakTokenExchangeResponse keycloakTokenExchangeResponse = exchangeToken(token);
        Jwt jwt = delegate.decode(keycloakTokenExchangeResponse.getAccessToken());

        if (useCache) {
            putJwtInCache(token, jwt);
        }

        return jwt;
    }

    private Optional<Jwt> getJwtFromCache(String token) {
        Cache<String, Jwt> tokenCache = cacheManager.getCache(TokenCacheConfiguration.TOKEN_CACHE_NAME);
        if (tokenCache.containsKey(token))
            return Optional.of(tokenCache.get(token));
        return Optional.empty();
    }

    private void putJwtInCache(String token, Jwt jwt) {
        Cache<String, Jwt> tokenCache = cacheManager.getCache(TokenCacheConfiguration.TOKEN_CACHE_NAME);
        LocalDateTime exp = LocalDateTime.ofInstant(jwt.getExpiresAt(), ZoneOffset.systemDefault());
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), exp);
        tokenCache.put(token, jwt, seconds, TimeUnit.SECONDS);
    }

    private KeycloakTokenExchangeResponse exchangeToken(String token) throws JsonProcessingException {
        log.info("Accessing keycloak to exchange the token");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        map.add("subject_token_type", "urn:ietf:params:oauth:token-type:access_token");
        map.add("client_id", keycloakTokenExchangeProperties.getClientId());
        map.add("client_secret", keycloakTokenExchangeProperties.getClientSecret());
        map.add("subject_token", token);
        map.add("subject_issuer", issuerUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> internalResponse = restTemplate.exchange(keycloakTokenExchangeProperties.getEndpoint(), HttpMethod.POST, entity, String.class);
        KeycloakTokenExchangeResponse oAuthResponse = objectMapper.readValue(internalResponse.getBody(), KeycloakTokenExchangeResponse.class);
        return oAuthResponse;
    }

}
