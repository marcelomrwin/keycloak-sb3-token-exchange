package com.redhat.rhbk.controller;


import java.util.HashMap;

import com.redhat.rhbk.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class IndexController {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    SecurityService securityService;

    @Value("${api.host.baseurl}")
    private String apiHost;

    @GetMapping(path = "/", produces = "application/json")
    public ResponseEntity<Jwt> index() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<Jwt>(securityService.getJWT(), httpHeaders, HttpStatus.OK);
    }

    @GetMapping(path = "/unauthenticated")
    public HashMap unauthenticatedRequests() {
        return new HashMap() {{
            put("this is ", "unauthenticated endpoint");
        }};
    }

    @GetMapping(path = "/client", produces = "application/json")
    public ResponseEntity<String> invokeClient() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + securityService.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> internalResponse = restTemplate.exchange(apiHost, HttpMethod.GET, entity, String.class);
        return ResponseEntity.ok(internalResponse.getBody());

    }


}
