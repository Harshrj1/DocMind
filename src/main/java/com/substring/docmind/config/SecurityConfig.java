package com.substring.docmind.config;

import com.substring.docmind.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter authenticationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity
    ) {


        //customization

        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                                        .requestMatchers(
                                                "/",
                                                "/error",
                                                "/health",
                                                "/api/v1/health",
                                                "/api/v1/auth/**",
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html").permitAll()
                                        .requestMatchers("/api/v1/admin/**").hasRole(Role.ADMIN.toString())
                                        .requestMatchers("/api/v1/**").hasAnyRole(Role.USER.toString(), Role.ADMIN.toString())
                                        .anyRequest().authenticated()
                )
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized: Authentication token is missing or expired\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\":false,\"message\":\"Forbidden: Access denied\"}");
                        })
                );

        return httpSecurity.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();

    }


    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) {
        return configuration.getAuthenticationManager();
    }

}
