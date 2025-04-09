package com.example.gatewayserver.config;
//import org.springframework.security.oauth2.jwt.Sup
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    SecurityWebFilterChain defaultSecurityFilterChain(ServerHttpSecurity http) throws Exception {
        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
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

                }).oauth2ResourceServer().jwt();
                http.cors().disable().csrf().disable();
        return http.build();
    }
}
