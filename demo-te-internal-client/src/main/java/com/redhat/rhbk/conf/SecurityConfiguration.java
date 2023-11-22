package com.redhat.rhbk.conf;

import java.security.interfaces.RSAPublicKey;

import com.redhat.rhbk.oauth2.KeycloakJwtAuthenticationConverter;
import com.redhat.rhbk.oauth2.KeycloakTokenExchangeJwtDecoder;
import com.redhat.rhbk.service.TokenService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Log
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final TokenService tokenService;

    @Value("${spring.security.oauth2.public-key.location}")
    private RSAPublicKey publicKey;

    @Autowired
    public SecurityConfiguration(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        JwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        return new KeycloakTokenExchangeJwtDecoder(tokenService, jwtDecoder);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().requestMatchers("/ping").permitAll()
                .requestMatchers("/internal").access("hasRole('ROLE_ONE')")
                .anyRequest().authenticated()
                .and()
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(CorsConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
                );
        return http.build();
    }

}
