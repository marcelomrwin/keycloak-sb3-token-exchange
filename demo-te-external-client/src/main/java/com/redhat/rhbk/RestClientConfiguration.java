package com.redhat.rhbk;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfiguration {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate rest = builder
                .setConnectTimeout(Duration.ofMillis(3000))
                .setReadTimeout(Duration.ofMillis(3000))
                .build();

//        rest.getInterceptors().add((request, body, execution) -> {
//
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication == null) {
//                return execution.execute(request, body);
//            }
//
//            if (!(authentication.getCredentials() instanceof AbstractOAuth2Token)) {
//                return execution.execute(request, body);
//            }
//
//            AbstractOAuth2Token token = (AbstractOAuth2Token) authentication.getCredentials();
//            request.getHeaders().setBearerAuth(token.getTokenValue());
//            return execution.execute(request, body);
//        });

        return rest;
    }


}
