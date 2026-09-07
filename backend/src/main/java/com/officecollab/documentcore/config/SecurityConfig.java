package com.officecollab.documentcore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * POC has no user auth (see project decision to drop Supabase magic-link/JWT auth entirely) -- every
 * request from the frontend acts as the single local user (see LocalUser). The only cross-origin concern
 * left is the browser's CORS check between the nginx-served frontend and this API on a different port.
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private final String allowedOrigin;

    public SecurityConfig(@Value("${app.cors.origin}") String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*");
    }
}
