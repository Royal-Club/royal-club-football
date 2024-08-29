package com.bjit.royalclub.royalclubfootball.util;

import com.bjit.royalclub.royalclubfootball.exception.JWTException;
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

    public boolean validateToken(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            if (!tokenEmail.equals(email) || isTokenExpired(token)) {
                throw new JWTException(INVALID_TOKEN, HttpStatus.EXPECTATION_FAILED);
            }
            return true;
        } catch (Exception e) {
            return false;
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
            throw new JWTException(INVALID_TOKEN, HttpStatus.EXPECTATION_FAILED);
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
