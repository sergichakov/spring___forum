package com.example.resourceserver.config;

//import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
import org.springframework.security.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtDecoders;
//import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
//import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

////////////////////////////////////////////////////////////////////////
@Configuration
@EnableWebSecurity
public class ResourceServerConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuerUri; //site JWT

/*    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        //RequestMatcher requestMatcher=new RegexRequestMatcher("/openapi/**","");

        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .anyRequest()
                                .anonymous()
                )
                .formLogin(withDefaults());






        return http.build();
    }               ///////////// */
/*    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
*/
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((auth)-> {//auth.requestMatchers("/resource/**").hasAuthority("SCOPE_resource.read");
                //try {

                    auth.requestMatchers("/v3/**", "/swagger-ui/**").anonymous()
                            .requestMatchers("/v3/**", "/swagger-ui/**").permitAll()
                            .requestMatchers("/login").anonymous()
                            .requestMatchers("/login").permitAll()
                    .requestMatchers("/resource").authenticated()
                                        ;





                            //.and().httpBasic().oauth2Login()

                            //.and().oauth2ResourceServer()
                            //.jwt();






                }).cors().disable();//.formLogin(withDefaults())
/*               .securityMatcher("/api/**")
                .authorizeHttpRequests(authz ->authz
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
         http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .anyRequest()
                                .authenticated()
                ).formLogin(withDefaults())         */
//https://stackoverflow.com/questions/78184759/spring-security-oauth2-resource-server-invalidbearertokenexception
                /*.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()).authenticationEntryPoint((request, response, exception) -> {
                    System.out.println("Authentication failed"); // here I set a break point and I get the cause of exception
                    BearerTokenAuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

                    delegate.commence(request, response, exception);
                }));*/
////.oauth2ResourceServer(oauth->oauth.jwt(Customizer.withDefaults())).formLogin(withDefaults());//new Customizer<OAuth2ResourceServerConfigurer<String>.JwtConfigurer>()));
        String jwksUri= "";









                //.accessTokenResponseClient(accessTokenResponseClient());

                //.securityMatcher("/view/**")///////////;





                //.mvcMatchers("/resource/**")
                //.authorizeRequests()
                //.mvcMatchers("/resource/**")





        return http.build();
    }
    //@Bean
/*    public SecurityFilterChain securityFilterChain2(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .anyRequest().anonymous()
                        .requestMatchers("/","/resource/**").anonymous()
                        .anyRequest().permitAll())
                .formLogin((form) -> form
                        .loginPage("/view/login")
                        .permitAll());
        //.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }/////////////////////////////////////////////////////*/

    UserDetailsService users() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /*@Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient =
                new DefaultAuthorizationCodeTokenResponseClient();
        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter =
                new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setAccessTokenResponseConverter(new CustomTokenResponseConverter());
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
                new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        accessTokenResponseClient.setRestOperations(restTemplate);
        log.error("log from OAuth2 Bean");
        return accessTokenResponseClient;
    }


    @Bean
    public JwtDecoder jwtDecoder() {
        // return your JWTdecoder
        return new NimbusJwtDecoder(new DefaultJWTProcessor<>());
    }
*///////////////////////////////
}