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
//    @Value("${token.signing.key}")

    //private String jwtSigningKey;

    public String extractState(String token){
        Claims claims=extractAllClaims(token);
        String state = claims.get("state",String.class);
        return state;
    }
    /**
     * Извлечение имени пользователя из токена
     *
     * @param token токен
     * @return имя пользователя
     */
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Генерация токена
     *
     * @param userDetails данные пользователя
     * @return токен
     */
//    public String generateToken(UserDetails userDetails, int liveMilliSec, String stateOfToken) {
//        Map<String, Object> claims = new HashMap<>();
//        if (userDetails instanceof AuthUser customUserDetails) {//AuthUser customUserDetails=(AuthUser)userDetails;//) {
//            claims.put("state", stateOfToken);
//            claims.put("id", customUserDetails.id());
//            claims.put("email", customUserDetails.getEmail());
//            claims.put("role", customUserDetails.getRole());
//            //claims.put("exp",new Date(System.currentTimeMillis() + 10 * 60 * 24 * 60 )); //100000 * 60 * 24 = 40 hours
//        }
//
//        System.out.println("generateToken claims="+claims);
//        return generateToken(claims, userDetails, liveMilliSec);
//    }

    /**
     * Проверка токена на валидность
     *
     * @param token       токен
     * @param userDetails данные пользователя
     * @return true, если токен валиден
     */
    public boolean isTokenValid(String token) {
//        final String userName = extractUserName(token);
        return  !isTokenExpired(token);
    }

    /**
     * Извлечение данных из токена
     *
     * @param token           токен
     * @param claimsResolvers функция извлечения данных
     * @param <T>             тип данных
     * @return данные
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    /**
     * Генерация токена
     *
     * @param extraClaims дополнительные данные
     * @param userDetails данные пользователя
     * @return токен
     */
//    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, int liveMilliSec) {
//        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + liveMilliSec))//+ 100000 * 60 * 24))
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
//    }

    /**
     * Проверка токена на просроченность
     *
     * @param token токен
     * @return true, если токен просрочен
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлечение даты истечения токена
     *
     * @param token токен
     * @return дата истечения
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлечение всех данных из токена
     *
     * @param token токен
     * @return данные
     */
    public Claims extractAllClaims(String token) {
        System.out.println("Oper JwtService token="+token+">>");
        Claims claims=null;
        try {
            //if (null==jwtService.extractAllClaims(jwtToken)){
            claims=Jwts.parser()
                    .verifyWith( getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
//            jwtService.extractAllClaims(jwtToken);
        }catch(SignatureException esignature){

            jwtKeyComponent.fetch(); // it is useful
//            LOGGER.debug("get HeaderUser need to be propagate to other - Posts");
            try {
                System.out.println("Oper getHeaderUserId= jwks "+jwtKeyComponent.get());
// it all just to new JWK key
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }///////////////maybe this try-catch is useless
            claims=Jwts.parser()
                    .verifyWith( getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
        //return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
//        return Jwts.parser()
//                .verifyWith( getSigningKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
        return claims;
    }

    /**
     * Получение ключа для подписи токена
     *
     * @return ключ
     */
    private PublicKey getSigningKey() {

        byte[] keyBytes = new byte[0];
        String jwtKey=null;
        try {
            jwtKey=jwtKeyComponent.get();
            log.debug("received JWK json "+jwtKey);
//            keyBytes = Decoders.BASE64.decode(jwtKeyComponent.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        PublicKey parsedRsaJwtKey=null;

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
        return parsedRsaJwtKey;//Keys.hmacShaKeyFor(keyBytes);
    }
}
