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

    private List<String> resolveOriginPatterns() {
        String[] allowedOrigins = cleanList(appProperties.getCors().getAllowedOrigins());
        List<String> patterns = new java.util.ArrayList<>();
        if (allowedOrigins.length > 0) {
            patterns.addAll(Arrays.asList(allowedOrigins));
        } else {
            patterns.add("*");
        }

        // If localhost or 127.0.0.1 is configured, automatically support any local dev port (e.g. 5173, 5174, etc.)
        boolean hasLocal = patterns.stream().anyMatch(p -> p.contains("localhost") || p.contains("127.0.0.1"));
        if (hasLocal) {
            if (!patterns.contains("http://localhost:*")) patterns.add("http://localhost:*");
            if (!patterns.contains("http://127.0.0.1:*")) patterns.add("http://127.0.0.1:*");
            if (!patterns.contains("https://localhost:*")) patterns.add("https://localhost:*");
            if (!patterns.contains("https://127.0.0.1:*")) patterns.add("https://127.0.0.1:*");
        }

        return patterns;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> originPatterns = resolveOriginPatterns();
        String[] allowedMethods = cleanList(appProperties.getCors().getAllowedMethods());
        String[] allowedHeaders = cleanList(appProperties.getCors().getAllowedHeaders());

        registry.addMapping("/**")
                .allowedOriginPatterns(originPatterns.toArray(String[]::new))
                .allowedMethods(allowedMethods.length > 0 ? allowedMethods : new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"})
                .allowedHeaders(allowedHeaders.length > 0 ? allowedHeaders : new String[]{"*"})
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> originPatterns = resolveOriginPatterns();
        String[] allowedMethods = cleanList(appProperties.getCors().getAllowedMethods());
        String[] allowedHeaders = cleanList(appProperties.getCors().getAllowedHeaders());

        for (String pattern : originPatterns) {
            configuration.addAllowedOriginPattern(pattern);
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
