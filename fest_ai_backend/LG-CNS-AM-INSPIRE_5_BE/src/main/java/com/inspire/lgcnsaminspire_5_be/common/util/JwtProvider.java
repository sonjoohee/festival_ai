package com.inspire.lgcnsaminspire_5_be.common.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

    @Value("${jwt.secret}") // .yml 에 등록된 키값을 사용할 수 있음.
    private String secret;

    private final long ACCESS_TOKEN_EXPIRY = 1000L * 60 * 30;
    private final long REFRESH_TOKEN_EXPIRY = 1000L * 60 * 60 * 24 * 7;

    private Key getSecretKey() {
        System.out.println("debug >>>> Provider jwt secret : " + secret);
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAT(Long userId, String loginId) {
        System.out.println("debug >>>> Provider createAT for loginId : " + loginId);

        return Jwts.builder()
                .setSubject(loginId)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .signWith(getSecretKey())
                .compact();
    }

    public String createRT(String loginId) {
        System.out.println("debug >>>> Provider createRT : ");

        return Jwts.builder()
                .setSubject(loginId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
                .signWith(getSecretKey())
                .compact();
    }

    // at를 통해서 subject 추출
    public String getLoginIdFromAT(String at) {
        System.out.println("debug >>>> Provider getLoginIdFromAT at : " + at);
        Claims claims = Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(at).getBody();

        System.out.println("debug >>>> Provider claims.getSubject : " + claims.getSubject());

        return claims.getSubject();
    }

}
