package com.inspire.lgcnsaminspire_5_be.common.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
    private static final long RT_TTL = 60 * 60 * 24 * 7; // Refresh Token 수명 7일
    private static final String REDIS_KEY_PREFIX = "RT:";

    // "RT:loginId"
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveToken(String loginId, String rt) {
        System.out.println(">>>> debug redis service save token for loginId: " + loginId);
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + loginId, rt, RT_TTL, TimeUnit.SECONDS);
    }

    public void deleteToken(String loginId) {
        System.out.println(">>>> debug redis service delete token for loginId: " + loginId);
        redisTemplate.delete(REDIS_KEY_PREFIX + loginId);
    }

    public String findByLoginId(String loginId) {
        System.out.println(">>>> debug redis service find token for loginId: " + loginId);
        Object token = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + loginId);
        return token != null ? token.toString() : null;
    }
}
