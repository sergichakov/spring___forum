package com.forum.jwk.service;

import com.forum.jwk.fetch.JwtKeyComponent;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.PublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@Service
public class JwtService {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JwtService.class);
    @Autowired
    private JwtKeyComponent jwtKeyComponent;

    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (SignatureException esignature) {

            jwtKeyComponent.fetch();

            try {
                System.out.println("Oper getHeaderUserId= jwks " + jwtKeyComponent.get());

            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
        return claims;
    }

    private PublicKey getSigningKey() {

        byte[] keyBytes = new byte[0];
        String jwtKey = null;
        try {
            jwtKey = jwtKeyComponent.get();
            log.debug("received JWK json " + jwtKey);

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        PublicKey parsedRsaJwtKey = null;

        try {
            parsedRsaJwtKey = RSAKey.parse(jwtKey).toPublicKey();
        } catch (ParseException e) {
            log.debug("cannot parse received jwtKey");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            log.debug("JoseException");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return parsedRsaJwtKey;
    }
}
