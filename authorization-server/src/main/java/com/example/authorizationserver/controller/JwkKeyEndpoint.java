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
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
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
    @Autowired
    private JWKSource<SecurityContext> jwkSource2;
    @Autowired
    private RSAKey jwkSource;
    @GetMapping("/jwks") ///.well-known/jwks.json"
    @ResponseBody
    public String getKey() {

//        Map<String,Object> jsonJwkSet=null;
//        jsonJwkSet=jwkSet.toJSONObject(true);
        jwkSet.toPublicJWKSet().getKeys();
        List<JWK> jwkValueList=jwkSet.toPublicJWKSet().getKeys();//(List<Object>)jsonJwkSet.get("keys");
        RSAPublicKey rsaPublicKey1=null;
        PublicKey publicKey=null;
        PublicKey rsaPublicKey=null;
        try {
            rsaPublicKey1=jwkSource.toRSAPublicKey();
            publicKey=jwkSource.toPublicKey();
            rsaPublicKey=jwkSource.toKeyPair().getPublic();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        String RSAbase=Encoders.BASE64.encode(rsaPublicKey.getEncoded());
        String jwtKey=(String)jwkSource.toPublicJWK().toString();
        PublicKey publicKeyJwtKey=null;
        try {
            publicKeyJwtKey=RSAKey.parse(jwtKey).toPublicKey();
        } catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println("topublicJWK"+jwtKey+"\npublic key="+publicKeyJwtKey.equals(publicKey));
        RSAKey parsedRsaJwtKey=null;
        PublicKey publicKeyFromParsedRsaJwtKey=null;
        try {
            parsedRsaJwtKey= RSAKey.parse(jwkValueList.get(0).toString());//jwtKey);
            publicKeyFromParsedRsaJwtKey=parsedRsaJwtKey.toPublicKey();
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        }
        System.out.println("public key "+ parsedRsaJwtKey.toString());
        //String jwtKeyString2="{\"kty\":\"RSA\",\"e\":\"AQAB\",\"kid\":\"1b20a99e-692a-432c-90b1-65c8061e5c24\",\"n\":\"0f7cq1nX50IJPo6LU6GHWHdYTvf_aSKJPA29l2IeE4bf8LN07n9zRDfB4VVVCM9E407X4sgwMwEZnIgamIkLIwRcv04rVfijTy1_iaipBeoAl8raykuK0vyonGbc6X__13dr5WYK0gUstBUIqZlrjBuI_mbMgt-r-4hK1LvcsWbDSeLcm8OyXgj13W6yIuTcxD6af3IWsdpddercbVFVay7ZvjsC1PzwCss3fbgXEQ_U19eW2GQU6aftGqWNbTlK3d_HFeU_XXkuAKuqyJL0B86tPLo-LODqYSQhmRbcvgF-mIFAQb8k3irnCO60HNIReIFkDqTGhFOciY4yk47WwQ\"}";
        //String token2="eyJraWQiOiIyOTZiZjA0My04NmMxLTRmMGQtOGJhNy1hZjNjOGE2ODE1YjAiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZCI6ImdhdGV3YXkiLCJuYmYiOjE3NDMyNTAzNTksInJvbGUiOiJST0xFX0FETUlOIiwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDMzMzY3NTksImlhdCI6MTc0MzI1MDM1OSwidXNlcklkIjoyfQ.GN_7eAq-VRj6j8E0XrjVgn3hwBqV2xgKfc67qENzyJzQqBFjr7znN_w7zbfqlMmJk3FojX2qG8iuwpeOwv9C7keZGNt9W5CIbg4zQ1yL_ZXTST_THzIrlQO3AozGmcLvOA-JkIfbVU3Vn5XFtJ3Xw_QjQqm_ekQoaPW2isXqGXgDFkHvLYVoByRrr6oM7MfjOpeE2y0fhqsOC-jWpX3XHsZL4j7K2V8BPxZdgzcPJiHBRkCAd2A7iUirKqWq5KsFfw8tXZonOn8rYx93oAvjLWLeHvgeg0KNftLMtf6yQjWSTmXfiN9Rpjhbpgx7AmogPc4ogJcX4xTf-yikRdcS1Q";
        String jwtKeyString3="{\"kty\":\"RSA\",\"e\":\"AQAB\",\"kid\":\"b5b6eae6-ee0b-4e70-b9a7-8d7a934f9647\",\"n\":\"oVuxhqsz2zoRBVpD2UASlKQzhy5r_4xuYZGechHqR9ZfcLdVG1MOypZXjwvzj-4WpopPKOriWlwkfKJ6PBUmMTbtUZopH5Yte15iru7tgPxKQoYdUiMlJVGSObytGb0AX35_gfg2b-k1g8iR6LiNMf2ozC_1SGAbkGMrE0zPKxKTNNLQ2mLNKvL13U7mXtytVB0nsAPix52aQ0jrGVLCg5purV2rDGRP02qsrOMEf9cj514piq3cOz4NCzNw8k5UmkOJKCg5BkqOp494I5nVjG8ypudJo_71pplcgmEbraMkI_opLH-RJa5SimA-RN68ktp6wMMmKi04aL3zoRMaqQ\"}";
        String token3="eyJraWQiOiI5Mjg4MzgzNC05ZjI0LTQyMWYtYTQ1MS0zODMyOTA3NjNlMjciLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZCI6ImdhdGV3YXkiLCJuYmYiOjE3NDMyNTg5MzUsInJvbGUiOiJST0xFX0FETUlOIiwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjkwMDAiLCJleHAiOjE3NDMzNDUzMzUsImlhdCI6MTc0MzI1ODkzNSwidXNlcklkIjoyfQ.oK0hSXAyTWGwXFN6aIiHgNQXBIn334cljQXYqjf4CVMN8b5y0O-FbE32Y3Q6Jj3XSCa8CArHtL3pIRQe_9vCipzPBz6iwNKRHBr0pA18NEIZ03jK6RcIgi7Nn6FxJ7Q6_kek931j6LJjOlxChH6fu49efCI-T0s1-wJVgmTVzdLQo6af8IJ1SYn-d3yNWgHm7Z6GuwBLYHaPhJbBrAD3gWxta7bo_VK6kqEptTFdQHhtU4MgqrDoKD67wFZHLWWbnQWTusrY7ID58qlaXfwKH8HiSq3HGuEK9HIq__BzWmRez8lwM_5QoLnK35dRA9Dde3NZWKBGYS4jhoq6j4Wo-g";
        //Jwts.parser().setSigningKey(publicKeyFromParsedRsaJwtKey).build().parseClaimsJws(token).getBody();
        RSAKey rsaKeyParsed=null;
        PublicKey publicKeyReal=null;
        byte[] bytesPublicKeyReal;
        try {
            rsaKeyParsed = RSAKey.parse(jwtKeyString3);
            System.out.println("rsaKeyParsed.getKeyId"+rsaKeyParsed.getKeyID());
            publicKeyReal=rsaKeyParsed.toPublicKey();
            bytesPublicKeyReal=publicKeyReal.getEncoded();
        } catch (JOSEException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        System.out.println("publicKeyReal:"+publicKeyReal.toString()+"\n bytesPublicKeyReal="+ Arrays.toString(bytesPublicKeyReal));
        try {
//            Jwts.parser().setSigningKey(publicKeyReal)//bytesPublicKeyReal)
//                    .build().parseClaimsJws(token3).getBody();
//            Jwts.parser()           //publicKeyReal
//                    .verifyWith(publicKeyReal)//jwkSource.toRSAPublicKey())//publicKeyReal)//publicKeyFromParsedRsaJwtKey)
//                    .build()
//                    .parseSignedClaims(token3)
//                    .getPayload();
        }catch(SignatureException  e){
            System.out.println("Oper signature failure");
            e.printStackTrace();
        }
        return jwkValueList.get(0).toString();//jwtKey;//64Key;
    }
    private JwtDecoder fakeGenKey(){
//        jwkSource2.
        JwtDecoder jwtDecoder=OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource2);
//        jwtDecoder.
        return jwtDecoder;
    }
    private Key getSignInKey(String SECRET_KEY,byte[] keyBytes){

        //byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
