package com.example.resourceserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
@ComponentScan("com.forum.filter")
public class ResourceServerConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.httpBasic();
//        http.cors().and()
//                .csrf().disable();
        http.csrf(CsrfConfigurer::disable).cors(CorsConfigurer::disable);
        http.authorizeHttpRequests((auth) -> {
                    auth.requestMatchers("/v3/**", "/swagger-ui/**").anonymous()
                            .requestMatchers("/v3/**", "/swagger-ui/**").permitAll()
                            .requestMatchers("/login").anonymous()
                            .requestMatchers("/login").permitAll()
                            .requestMatchers("/resource").authenticated();
//                    auth.anyRequest().anonymous();auth.anyRequest().permitAll();

                });//.cors().disable().csrf().disable();//.httpBasic();
//                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()).authenticationEntryPoint((request, response, exception) -> {
//                    System.out.println("Authentication failed"); // here I set a break point and I get the cause of exception
//                    BearerTokenAuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
//
//                    delegate.commence(request, response, exception);
//                }));
        return http.build();
    }
//    @Bean
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .mvcMatcher("/resource/**")
//                .authorizeRequests()
//                .mvcMatchers("/resource/**")
//                .access("hasAuthority('SCOPE_resource.read')")
//                .and()
//                .oauth2ResourceServer()
//                .jwt();
//        return http.build();
//    }

}