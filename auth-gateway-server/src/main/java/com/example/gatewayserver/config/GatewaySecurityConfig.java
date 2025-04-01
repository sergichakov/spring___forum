package com.example.gatewayserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import static org.springframework.security.config.Customizer.withDefaults;
//@EnableWebSecurity
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
    @Bean
    SecurityWebFilterChain defaultSecurityFilterChain(ServerHttpSecurity http) throws Exception {
        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
//                http.securityContext(securityContext -> securityContext.
//                securityContextRepository(new HttpSessionSecurityContextRepository())
//        )
                .authorizeExchange(exchanges -> {
                    exchanges.pathMatchers("/login").permitAll();
                    exchanges.pathMatchers("/oauth2/**").permitAll();
                    exchanges.pathMatchers("/swagger-ui/**").permitAll();
                    exchanges.pathMatchers("/api/**").permitAll();
                    exchanges.pathMatchers("/dirictoriesweb").hasRole("ADMIN");
                    exchanges.pathMatchers("/dirictoriesweb/**").hasRole("ADMIN");
                    exchanges.pathMatchers("/topicsweb").authenticated();
                    exchanges.pathMatchers("/topicsweb/**").authenticated();
                    exchanges.pathMatchers("/postsweb").authenticated();
                    exchanges.pathMatchers("/postsweb/**").authenticated();
                    exchanges.anyExchange().authenticated();
                    ////exchanges.anyExchange().permitAll();

                }).oauth2ResourceServer()
                .jwt();

//                .authorizeRequests(authorizeRequests ->{
//                        authorizeRequests
//                                .anyRequest()
//                                .anonymous();
//                authorizeRequests.anyRequest().permitAll();
//                })
                http.cors().disable().csrf().disable(); //An expected CSRF token cannot be found
                //.formLogin(withDefaults());//.cors().disable()   //// I added it after
        ////.csrf().disable().headers().frameOptions().disable();
        return http.build();
    }
}
