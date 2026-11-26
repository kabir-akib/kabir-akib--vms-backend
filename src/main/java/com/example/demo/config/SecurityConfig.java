package com.example.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter;
    private final HandlerExceptionResolver exceptionResolver;

    public SecurityConfig(
            BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.bearerTokenAuthenticationFilter = bearerTokenAuthenticationFilter;
        this.exceptionResolver = exceptionResolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                exceptionResolver.resolveException(request, response, null, authException))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                exceptionResolver.resolveException(request, response, null, accessDeniedException))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/health", "/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/v1/vehicles/**", "/api/v1/auth/logout", "/api/v1/auth/me").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(bearerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
