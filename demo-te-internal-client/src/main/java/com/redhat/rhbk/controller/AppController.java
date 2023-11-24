package com.redhat.rhbk.controller;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.rhbk.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AppController {

    @Autowired
    TokenService tokenService;
    @Autowired
    ObjectMapper objectMapper;

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping(value = "/internal", produces = "application/json")
    public ResponseEntity<?> internalService(@RequestHeader(value = "Authorization") String authorizationHeader) {
        Jwt headerJwt = tokenService.getJwtFromAuthorizationHeader(authorizationHeader);
        Jwt contextJwt = tokenService.getContextJwt();

        Map<String, Map<String, Object>> response = new HashMap<>();
        Map<String, Object> headerJwtMap = objectMapper.convertValue(headerJwt, new TypeReference<Map<String, Object>>() {
        });
        headerJwtMap.remove("tokenValue");
        Map<String, Object> contextJwtMap = objectMapper.convertValue(contextJwt, new TypeReference<Map<String, Object>>() {
        });
        contextJwtMap.remove("tokenValue");

        response.put("original-token", headerJwtMap);
        response.put("exchange-token", contextJwtMap);

        return ResponseEntity.ok(response);
    }

}
