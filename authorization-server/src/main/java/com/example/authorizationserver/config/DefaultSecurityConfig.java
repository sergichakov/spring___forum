package com.example.authorizationserver.config;

import com.example.authorizationserver.service.CustomUserDetailsService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;


//import javax.servlet.http.HttpServletRequest;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class DefaultSecurityConfig {
    /*@Value("${auth-provider.issuer-uri}")
    private String issuerUri;*/
    @Autowired
    private CustomUserDetailsService userDetailsService;
/**/
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        ////https://stackoverflow.com/questions/49201779/spring-boot-do-not-send-hsts-header
        http.headers().httpStrictTransportSecurity().disable().and()
                .csrf(CsrfConfigurer::disable)// An expected CSRF token cannot be found
                .authorizeHttpRequests(authorizeRequests -> {
                            authorizeRequests.requestMatchers("/jwks").permitAll();
                            authorizeRequests.requestMatchers("/oauth2").permitAll();
                            authorizeRequests.requestMatchers("/oauth2/**").permitAll();
                            authorizeRequests.requestMatchers("/login").permitAll();
                            authorizeRequests
                                    .anyRequest()
                                    .authenticated();
                        }
                );
        //        .oauth2ResourceServer().jwt();
        http
                // .oauth2ResourceServer(oauth2 -> oauth2
                //         .jwt(jwt -> jwt///////////////////"http://authorization-server:9000/jwks.json"
                //                 .jwkSetUri("http://localhost:9000/jwks.json")
                //         )
                //)
                .formLogin(withDefaults());

        return http.build();
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        //return NoOpPasswordEncoder.getInstance();
        return PASSWORD_ENCODER;
    }
    /*
    @Bean
    ReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder jwtDecoder = (NimbusReactiveJwtDecoder) ReactiveJwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }
    public static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", null);

        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (jwt.getAudience().contains("messaging")) {
                return OAuth2TokenValidatorResult.success();
            } else {
                return OAuth2TokenValidatorResult.failure(error);
            }
        }
    }
   @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
    @Bean
    public JWKSet getJwkSet(){
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return jwkSet;
    }
    @Bean
    public JWKSource<SecurityContext> jwkSource(JWKSet jwkSet) {


        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }
*/
}
