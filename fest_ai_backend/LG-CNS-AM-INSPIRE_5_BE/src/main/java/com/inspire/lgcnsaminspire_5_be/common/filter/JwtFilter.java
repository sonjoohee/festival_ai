package com.inspire.lgcnsaminspire_5_be.common.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Jwt version 일 때 사용
//@Component
public class JwtFilter implements Filter {

    @Value("${jwt.secret}") // .yml 에 등록된 키값을 사용할 수 있음.
    private String secret;
    private Key key;

    // private final long ACCESS_TOKEN_EXPIRY = 1000L * 60 * 30 ;
    // private final long REFRESH_TOKEN_EXPIRY = 1000L * 60 * 60 * 24 * 7 ;
    @PostConstruct
    private void init() {
        System.out.println("debug >>>> Provider jwt secret : " + secret);
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static final List<String> WHITE_LIST = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/auth/signup",
            "/api/auth/login",
            "/api/festivals/**");

    private final AntPathMatcher matcher = new AntPathMatcher();

    public boolean isPath(String path) {
        return WHITE_LIST.stream()
                .anyMatch(pattern -> matcher.match(pattern, path));
    }

    @Override
    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        System.out.println("debug >>>> JwtFilter doFilter");

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        res.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Refresh-token");
        res.setHeader("Access-Control-Allow-Credentials", "true");

        String endPoint = req.getRequestURI();
        System.out.println("debug >>>> JwtFilter user request path(endPoint) : " + endPoint);
        String method = req.getMethod();
        System.out.println("debug >>>> JwtFilter user request method : " + method);

        // preflight options 로 전달이 이루어질 때만 동작
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            System.out.println("debug >>>> JwtFilter preflight OPTIONS ");

            // header set : Origin, Method, Header
            res.setStatus(HttpServletResponse.SC_OK);
            res.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Refresh-token");
            res.setHeader("Access-Control-Allow-Credentials", "true");

            chain.doFilter(request, response);
            return;
        }

        if (isPath(endPoint)) {
            System.out.println("debug >>>> JwtFilter " + endPoint + " 는 토큰없이 통과");
            chain.doFilter(request, response);
            return;
        }

        // white list 등록되지않은 endPoint 접근이 발생한다면?
        // request header 에 심어져있는 token 검증(만료, 서명이 맞는지)
        String header = req.getHeader("Authorization");
        System.out.println("debug >>>> JwtFilter header " + header);
        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("debug >>>> JwtFilter Not Authorization");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String token = header.substring(7);
        System.out.println("debug >>>> JwtFilter token : " + token);
        System.out.println("debug >>>> JwtFilter token validation ");
        System.out.println("debug >>>> Payload(Claims) == token 추출");

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            System.out.println("debug >>>> JwtFilter token validation success move to ctrl");
            chain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println("debug >>>> JwtFilter token validation fail");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    }
}
