package com.example.authorizationserver.config;

import com.example.authorizationserver.model.UserEntity;
import com.example.authorizationserver.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {
    @Value("${auth-provider.issuer-uri}")
    private String issuer_uri;
    @Autowired
    private UserRepository userRepository;
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            //JwsHeader.Builder headers = context.getJwsHeader();
            JwtClaimsSet.Builder claims = context.getClaims();
            if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
                UsernamePasswordAuthenticationToken authentication = context.getPrincipal();
                User user = (User) authentication.getPrincipal();

                UserEntity userEntity=userRepository.findByLogin(user.getUsername());
                //user.getAuthorities().
                // Customize headers/claims for access_token
                claims.claim("userId", userEntity.getId());//((CustomUser )user).getId);
                claims.claim("role","ROLE_"+userEntity.getUserRole());
            } //else if (context.getTokenType().getValue().equals(OidcParameterNames.ID_TOKEN)) {
                // Customize headers/claims for id_token

            //}
        };
    }
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.formLogin(Customizer.withDefaults()).build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("gateway")////clientId("gateway") .clientId("test-client")
                .clientSecret("{noop}secret") ////clientSecret("{noop}secret") .clientSecret("{noop}test-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/swagger-ui/index.html") //"http://127.0.0.1:8080/login/oauth2/code/gateway")
                .scope(OidcScopes.OPENID)
                .scope("resource.write")
                .scope("resource.read")
//                .scope("message.read")
//                .scope("message.write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.of(60*24, ChronoUnit.MINUTES))
                        .refreshTokenTimeToLive(Duration.of(120*24, ChronoUnit.MINUTES))
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }
    @Bean
    public JWKSet getJwkSet(){
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return jwkSet;
    }
    @Bean
    public JWKSource<SecurityContext> jwkSource(JWKSet jwkSet) {
//        RSAKey rsaKey = generateRsa();
//        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }
/////////////////////////////////////////////////////    @Bean
    private static RSAKey generateRsa() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        // delete next line if you can
        System.out.println("Oper RSA key "+keyPair.getPublic() );
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public ProviderSettings providerSettings() {
        return ProviderSettings.builder()
                .issuer(issuer_uri) //"http://localhost:9000") ////
                .build();
    }
    @Bean
    private static RSAKey fakePermanentRsa()  {
        //KeyPair keyPair = generateRsaKey();
        //complement JavascriptTokenKey"eyJraWQiOiI5Mjg4MzgzNC05ZjI0LTQyMWYtYTQ1MS0zODMyOTA3NjNlMjciLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZCI6ImdhdGV3YXkiLCJuYmYiOjE3NDMyNTg5MzUsInJvbGUiOiJST0xFX0FETUlOIiwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDMzNDUzMzUsImlhdCI6MTc0MzI1ODkzNSwidXNlcklkIjoyfQ.oK0hSXAyTWGwXFN6aIiHgNQXBIn334cljQXYqjf4CVMN8b5y0O-FbE32Y3Q6Jj3XSCa8CArHtL3pIRQe_9vCipzPBz6iwNKRHBr0pA18NEIZ03jK6RcIgi7Nn6FxJ7Q6_kek931j6LJjOlxChH6fu49efCI-T0s1-wJVgmTVzdLQo6af8IJ1SYn-d3yNWgHm7Z6GuwBLYHaPhJbBrAD3gWxta7bo_VK6kqEptTFdQHhtU4MgqrDoKD67wFZHLWWbnQWTusrY7ID58qlaXfwKH8HiSq3HGuEK9HIq__BzWmRez8lwM_5QoLnK35dRA9Dde3NZWKBGYS4jhoq6j4Wo-g"
        String jwtKeyString="{\"kty\":\"RSA\",\"e\":\"AQAB\",\"kid\":\"b5b6eae6-ee0b-4e70-b9a7-8d7a934f9647\",\"n\":\"oVuxhqsz2zoRBVpD2UASlKQzhy5r_4xuYZGechHqR9ZfcLdVG1MOypZXjwvzj-4WpopPKOriWlwkfKJ6PBUmMTbtUZopH5Yte15iru7tgPxKQoYdUiMlJVGSObytGb0AX35_gfg2b-k1g8iR6LiNMf2ozC_1SGAbkGMrE0zPKxKTNNLQ2mLNKvL13U7mXtytVB0nsAPix52aQ0jrGVLCg5purV2rDGRP02qsrOMEf9cj514piq3cOz4NCzNw8k5UmkOJKCg5BkqOp494I5nVjG8ypudJo_71pplcgmEbraMkI_opLH-RJa5SimA-RN68ktp6wMMmKi04aL3zoRMaqQ\"}";
        RSAKey rsaKey=null; String keyId=null;
        try {
            rsaKey=RSAKey.parse(jwtKeyString);
            keyId=rsaKey.getKeyID();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        RSAPublicKey publicRSAKey = null;//keyPair.getPublic();
        PublicKey publicKey=null;
        try {
            publicRSAKey = (RSAPublicKey) rsaKey.toRSAPublicKey();
            publicKey=rsaKey.toPublicKey();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        // delete next line if you can
        System.out.println("Oper RSA New key "+publicRSAKey );
        //RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicRSAKey)
                //.privateKey(privateKey)
                .keyID(keyId)//UUID.randomUUID().toString())
                .build();
    }
}