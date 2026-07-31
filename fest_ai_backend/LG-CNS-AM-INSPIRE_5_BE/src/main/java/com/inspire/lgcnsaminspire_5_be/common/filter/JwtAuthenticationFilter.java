package com.inspire.lgcnsaminspire_5_be.common.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}") // .yml 에 등록된 키값을 사용할 수 있음.
    private String secret;
    private Key key;

    @PostConstruct
    private void init() {
        System.out.println("debug >>>> Provider jwt secret : " + secret);
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws IOException, ServletException {
        System.out.println("debug >>>> JwtAuthenticationFilter doFilterInternal");

        String endPoint = request.getRequestURI();
        System.out.println("debug >>>> JwtAuthenticationFilter user request path(endPoint) : " + endPoint);
        String method = request.getMethod();
        System.out.println("debug >>>> JwtAuthenticationFilter user request method : " + method);

        // preflight options 로 전달이 이루어질 때만 동작
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("debug >>>> JwtAuthenticationFilter preflight OPTIONS ");

            // 환경정보를 SecurityConfig - configuration 통해서 전달
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        System.out.println("debug >>>> JwtAuthenticationFilter header " + header);
        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("debug >>>> JwtAuthenticationFilter Not Authorization");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        System.out.println("debug >>>> JwtAuthenticationFilter token : " + token);
        System.out.println("debug >>>> JwtAuthenticationFilter token validation ");
        System.out.println("debug >>>> Payload(Claims) == token 추출");

        try {
            // Claims == JMT 데이터(header, payload, sign)
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String loginId = claims.getSubject();
            System.out.println("debug >>>> JwtAuthenticationFilter claims get loginId : " + loginId);

            // 토큰에서 userId 추출
            Long userId = claims.get("userId", Long.class);
            System.out.println("debug >>>> JwtAuthenticationFilter claims get userId : " + userId);

            // 토큰에서 role 추출
            String role = claims.get("role", String.class);
            System.out.println("debug >>>> JwtAuthenticationFilter claims get role : " + role);

            // 재할당을 피하기 위해 새로운 final 변수(상수)에 값 할당
            final String finalRole = (role == null) ? "USER" : role;

            //
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(() -> "ROLE_" + finalRole));

            // 요청한 사용자와 인증정보 객체 연동
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 사용자의 정보를 SecurityContextHolder 저장할 수 있고
            // 필요한 경우 ctrl, service 가 꺼내서 사용할 수 있음
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            System.out.println("debug >>>> JwtAuthenticationFilter: Security 인증 객체 생성 완료");

            System.out.println("debug >>>> JwtAuthenticationFilter context holder setAuthentication");

        } catch (Exception e) {
            System.out.println("debug >>>> JwtAuthenticationFilter: 만료되거나 변조된 잘못된 토큰 처리 에러");
            // 토큰 검증이 실패해도 permitAll 경로는 익명으로 통과시키고,
            // 보호된 경로는 뒤쪽 인가 단계(anyRequest().authenticated())에서 막히도록 SecurityContext를 비워줍니다.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
