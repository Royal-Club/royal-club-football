package com.bjit.royalclub.royalclubfootball.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.FORBIDDEN_ERROR_MESSAGE;

@Component
public class JwtUnAuthorizedResponseAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        Map<String, Object> data = new HashMap<>();
        data.put("timeStamp", System.currentTimeMillis());
        data.put("status", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        data.put("statusCode", HttpStatus.UNAUTHORIZED.value());
        data.put("message", FORBIDDEN_ERROR_MESSAGE);

        response.getOutputStream().println(objectMapper.writeValueAsString(data));
    }
}
