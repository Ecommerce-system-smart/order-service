package com.ecommerce.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Base64;
import java.util.Optional;

@Configuration
public class AuditorConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String[] parts = token.split("\\.");
                        if (parts.length >= 2) {
                            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(payload);
                            if (node.has("preferred_username")) {
                                return Optional.ofNullable(node.get("preferred_username").asText());
                            } else if (node.has("sub")) {
                                return Optional.ofNullable(node.get("sub").asText());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Fallback
            }
            return Optional.of("system");
        };
    }
}
