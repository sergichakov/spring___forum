package com.forum.directory.web.config;

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
@ComponentScan("com.forum.filter")
public class DirectoryWebSecurityConfig {
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(CsrfConfigurer::disable).cors(CorsConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->{
//                    authorizeRequests
//                            .anyRequest()
//                            .anonymous();
                    authorizeRequests.anyRequest().permitAll();
                });
//                .cors().disable().csrf().disable();
        //.formLogin(withDefaults());//.cors().disable()   //// I added it after
        ////.csrf().disable().headers().frameOptions().disable();
        return http.build();
    }
}
