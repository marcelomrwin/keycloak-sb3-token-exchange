package com.redhat.rhbk;


import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    @GetMapping(path = "/")
    public HashMap index() {
        // get a successful user login
        OAuth2User user = ((OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return new HashMap() {{
            put("message", "Congratulations! You're authenticated");
            put("hello", user.getAttribute("name"));
            put("your email is", user.getAttribute("email"));
            put("Next", "Call GET http://localhost:80801/client");
        }};
    }

    @GetMapping(path = "/unauthenticated")
    public HashMap unauthenticatedRequests() {
        return new HashMap() {{
            put("this is ", "unauthenticated endpoint");
        }};
    }

    @GetMapping("/client")
    public ResponseEntity<String> invokeClient() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + securityService.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> internalResponse = restTemplate.exchange(apiHost, HttpMethod.GET, entity, String.class);
        return ResponseEntity.ok(internalResponse.getBody());

    }


}
