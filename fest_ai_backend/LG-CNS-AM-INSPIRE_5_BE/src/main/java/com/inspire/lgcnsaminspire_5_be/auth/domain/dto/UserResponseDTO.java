package com.inspire.lgcnsaminspire_5_be.auth.domain.dto;

import com.inspire.lgcnsaminspire_5_be.auth.domain.entity.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long userId;
    private String name;
    private String token;

    // Entity -> Response DTO 변환 메서드
    // 로그인 성공 시점에 토큰을 주입받아 응답 객체 빌드
    public static UserResponseDTO fromEntity(UserEntity entity, String generatedToken) {
        return UserResponseDTO.builder()
                .userId(entity.getUserId())
                .name(entity.getName())
                .token(generatedToken) // 로그인 시점에 발급된 토큰 바인딩
                .build();
    }
}
