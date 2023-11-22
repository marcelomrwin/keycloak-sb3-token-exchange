package com.redhat.rhbk.oauth2;

import com.redhat.rhbk.service.TokenService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

public class KeycloakTokenExchangeJwtDecoder implements JwtDecoder {

    private final TokenService tokenService;

    private final JwtDecoder delegate;

    public KeycloakTokenExchangeJwtDecoder(TokenService tokenService, JwtDecoder jwtDecoder) {
        this.tokenService = tokenService;
        this.delegate = jwtDecoder;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            Jwt jwt = tokenService.getJwt(token, delegate);
            return jwt;
        } catch (Exception e) {
            throw new JwtException(e.getMessage());
        }
    }
}
