package com.forum.post.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
@EnableWebSecurity
@Configuration
@ComponentScan({"com.forum.filter", "com.forum.jwk.fetch", "com.forum.jwk.service","com.forum.config"})
public class PostWebSecurityConfig {
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(CsrfConfigurer::disable).cors(CorsConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->{
                    authorizeRequests.anyRequest().permitAll();
                }).oauth2ResourceServer().jwt();

        return http.build();
    }
}
