package com.inspire.lgcnsaminspire_5_be.common.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// jwt version에선 필터를 사용하므로 없어도됨
//@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedOriginPatterns("http://localhost:3000")
                .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS", "PATCH");

    }
}
