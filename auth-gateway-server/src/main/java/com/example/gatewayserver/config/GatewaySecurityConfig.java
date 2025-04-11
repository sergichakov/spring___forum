package com.example.gatewayserver.config;
//import org.springframework.security.oauth2.jwt.Sup
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
    private String jwkSetUri="http://authorization-server:9000/oauth2/jwks";
    private String issuerUri="http://authorization-server:9000";
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

                })//.oauth2ResourceServer().jwt();

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(//tw -> tw.jwkSetUri(""))
                                withDefaults())

                );
                http.cors().disable().csrf().disable();
        return http.build();
    }
//    https://stackoverflow.com/questions/70358027/webflux-cant-authenticate-jwt-token-from-azure-b2c

/*    ReactiveJwtDecoder customDecoder() {
        HttpClient httpClient =
                HttpClient.create()
                        .proxy(proxy -> proxy
                                .type(ProxyProvider.Proxy.HTTP)
                                .host(proxyHost)
                                .port(proxyPort));
        ReactorClientHttpConnector conn = new ReactorClientHttpConnector(httpClient);

        // use a customized webClient with explicit proxy settings for the profile
        final NimbusReactiveJwtDecoder userTokenDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(this.jwkSetUri)
                .webClient(WebClient.builder().clientConnector(conn).build()).build();

        // add both issuer and timestamp validators for JWT token
        OAuth2TokenValidator<Jwt> jwtValidator =
                JwtValidators.createDefaultWithIssuer(issuerUri);
        userTokenDecoder.setJwtValidator(jwtValidator);

        return userTokenDecoder;
    }
*/
    //https://stackoverflow.com/questions/76120561/spring-security-6-oauth2-custom-validator
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder jwtDecoder= NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

//        NimbusReactiveJwtDecoder jwtDecoder3 = NimbusReactiveJwtDecoder.withJwkSetUri(this.jwkSetUri);
/*        ReactiveJwtDecoder jwtDecoder = //(NimbusReactiveJwtDecoder)
                ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
*/
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        //OAuth2TokenValidator<Jwt> withOperation = new OperationClaimValidator();

        jwtDecoder.setJwtValidator(  new DelegatingOAuth2TokenValidator<>(withIssuer));//, withOperation)  );
//        jwtDecoder.setJwtValidator(withIssuer);
        return jwtDecoder;
    }
}
