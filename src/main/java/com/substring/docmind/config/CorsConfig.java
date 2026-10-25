package com.substring.docmind.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    private String[] cleanList(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new String[0];
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = cleanList(appProperties.getCors().getAllowedOrigins());
        String[] allowedMethods = cleanList(appProperties.getCors().getAllowedMethods());
        String[] allowedHeaders = cleanList(appProperties.getCors().getAllowedHeaders());

        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.length > 0 ? allowedOrigins : new String[]{"*"})
                .allowedMethods(allowedMethods.length > 0 ? allowedMethods : new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"})
                .allowedHeaders(allowedHeaders.length > 0 ? allowedHeaders : new String[]{"*"})
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String[] allowedOrigins = cleanList(appProperties.getCors().getAllowedOrigins());
        String[] allowedMethods = cleanList(appProperties.getCors().getAllowedMethods());
        String[] allowedHeaders = cleanList(appProperties.getCors().getAllowedHeaders());

        if (allowedOrigins.length > 0) {
            for (String origin : allowedOrigins) {
                configuration.addAllowedOriginPattern(origin);
            }
        } else {
            configuration.addAllowedOriginPattern("*");
        }

        if (allowedMethods.length > 0) {
            configuration.setAllowedMethods(List.of(allowedMethods));
        } else {
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }

        if (allowedHeaders.length > 0) {
            configuration.setAllowedHeaders(List.of(allowedHeaders));
        } else {
            configuration.setAllowedHeaders(List.of("*"));
        }

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
