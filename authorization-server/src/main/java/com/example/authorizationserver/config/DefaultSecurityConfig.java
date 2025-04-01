package com.example.authorizationserver.config;

import com.example.authorizationserver.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;


import javax.servlet.http.HttpServletRequest;

import static org.springframework.security.config.Customizer.withDefaults;
@Configuration
@EnableWebSecurity//////////////////////////////////////////////////////////////
//
public class DefaultSecurityConfig {
    @Autowired
    private CustomUserDetailsService userDetailsService;
/**/
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        ////https://stackoverflow.com/questions/49201779/spring-boot-do-not-send-hsts-header
        http.headers().httpStrictTransportSecurity().disable().and()
                .csrf(CsrfConfigurer::disable)// An expected CSRF token cannot be found
                //.antMatcher("/jwks").anonymous().and()
                .authorizeRequests(authorizeRequests ->{
                    authorizeRequests.antMatchers("/jwks").permitAll();//.hasAnyAuthority()..permitAll();

                        authorizeRequests
                                .anyRequest()
                                .authenticated();//.authenticated(); было так потом попробовал по другому
                    }
                )
                .oauth2ResourceServer().jwt();http
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt///////////////////"http://authorization-server:9000/jwks.json"
//                                .jwkSetUri("http://localhost:9000/jwks.json")
//                        )
//                )
                .formLogin(withDefaults());//".cors().disable()   //// I added it after
                ////.csrf().disable().headers().frameOptions().disable();
        return http.build();
    }

//    @Bean
    UserDetailsService users() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        //return NoOpPasswordEncoder.getInstance();
        return PASSWORD_ENCODER;
    }

}
