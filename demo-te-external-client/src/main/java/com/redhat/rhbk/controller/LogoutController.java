package com.redhat.rhbk.controller;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostMapping(path = "/sso-logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public HashMap logout(@RequestParam("logout_token") String logoutToken, HttpServletRequest request) throws Exception {

        String[] splitString = logoutToken.split("\\.");
        String body = new String(java.util.Base64.getDecoder().decode(splitString[1]));

        ObjectMapper mapper = new ObjectMapper();
        HashMap bodyMap = mapper.readValue(body, HashMap.class);
        logger.debug("logging out {}", bodyMap.get("sid"));
        doLogout(request);

        return new HashMap() {{
            put("status", true);
        }};
    }

    @GetMapping("/forceLogout")
    public ResponseEntity<?> forceLogout(HttpServletRequest request) {
        doLogout(request);
        return ResponseEntity.ok().build();
    }

    private void doLogout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        SecurityContextHolder.clearContext();
        if (session != null) session.invalidate();

        for(Cookie cookie : request.getCookies()) {
            cookie.setMaxAge(0);
        }
    }
}
