package com.inspire.lgcnsaminspire_5_be.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inspire.lgcnsaminspire_5_be.auth.domain.dto.UserLoginRequestDTO;
import com.inspire.lgcnsaminspire_5_be.auth.domain.dto.UserRequestDTO;
import com.inspire.lgcnsaminspire_5_be.auth.domain.dto.UserResponseDTO;
import com.inspire.lgcnsaminspire_5_be.auth.domain.entity.UserEntity;
import com.inspire.lgcnsaminspire_5_be.auth.repository.UserRepository;
import com.inspire.lgcnsaminspire_5_be.common.service.RedisService;
import com.inspire.lgcnsaminspire_5_be.common.util.JwtProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean signUp(UserRequestDTO request) {
        System.out.println(">>>> debug user service signUp ");

        // 아이디 중복 체크
        if (userRepository.existsByLoginId(request.getLoginId())) {
            System.out.println(">>>> debug user service signUp: 아이디 중복으로 가입 실패");
            return false;
        }

        UserEntity entity = UserRequestDTO.toEntity(request);
        userRepository.save(entity);

        return true;
    }

    public UserResponseDTO signIn(UserLoginRequestDTO request) {
        System.out.println(">>>> debug user service signIn ");

        // UserRepository loginId로 유저 엔티티 수집
        UserEntity entity = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        // 비밀번호 매칭 검증
        if (!passwordEncoder.matches(request.getPassword(), entity.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 발행
        String at = jwtProvider.createAT(entity.getUserId(), entity.getLoginId());
        // String rt = at; // MVP 단위 정합성을 위한 바인딩

        System.out.println(">>>> debug user service at :  " + at);

        // Redis 토큰 관리 규칙 적용
        // System.out.println(">>>> debug user service RT redis save");
        // redisService.saveToken(entity.getLoginId(), rt);

        // 🌟 명세서 규격: AuthResponseDTO 내부의 fromEntity가 AuthEntity를 받도록 처리
        return UserResponseDTO.fromEntity(entity, at);
    }

    // redis token delete
    public void signOut(String at) {
        System.out.println(">>>> debug user service signout at : " + at);
        // String loginId = jwtProvider.getLoginIdFromAT(at);
        // redisService.deleteToken(loginId);
    }
}
