package com.example.authorizationserver.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.io.Encoders;
import com.nimbusds.jose.proc.SecurityContext;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;

@RestController
public class JwkKeyEndpoint {
    @Autowired
    private JWKSet jwkSet;

    @GetMapping("/jwks")
    @ResponseBody
    public String getKey() {
        jwkSet.toPublicJWKSet().getKeys();
        List<JWK> jwkValueList = jwkSet.toPublicJWKSet().getKeys();
        return jwkValueList.get(0).toString();
    }
}
