package com.inspire.lgcnsaminspire_5_be.auth.domain.dto;

import com.inspire.lgcnsaminspire_5_be.auth.domain.entity.UserEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class UserRequestDTO {
    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Size(max = 50)
    private String name;

    // DTO -> Entity 변환 메서드
    public static UserEntity toEntity(UserRequestDTO request) {
        return UserEntity.builder()
                .loginId(request.getLoginId())
                .password(request.getPassword())
                .name(request.getName())
                .build();
    }
}
