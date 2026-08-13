package com.bjit.royalclub.royalclubfootball.util;

import com.bjit.royalclub.royalclubfootball.exception.SecurityException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.INVALID_TOKEN;

@Service
public class JWTUtil {

    private static final String ROLES_KEY = "roles";
    private final Key key;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    public JWTUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, List<String> roles) {
        Map<String, Object> claims = Map.of(ROLES_KEY, roles);
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Access-token lifetime in seconds, for clients that renew ahead of expiry. */
    public long getExpirationSeconds() {
        return jwtExpirationInMs / 1000;
    }

    /**
     * Subject of a token that is well-formed, correctly signed and unexpired; {@code null} for
     * anything else.
     * <p>
     * Never throws. An expired token is an ordinary event - it is what every client hits once a week
     * - and the caller has to answer it with a 401 rather than let an exception escape the filter
     * chain, where no {@code @ControllerAdvice} can reach it.
     */
    public String emailIfValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                return null;
            }
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> {
            List<?> rawRoles = claims.get(ROLES_KEY, List.class);
            if (rawRoles == null) {
                return Collections.emptyList();
            }
            return rawRoles.stream()
                    .map(Object::toString)
                    .toList();
        });
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new SecurityException(INVALID_TOKEN, HttpStatus.EXPECTATION_FAILED);
        }
    }

}
