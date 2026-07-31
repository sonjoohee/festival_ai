package com.inspire.lgcnsaminspire_5_be.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inspire.lgcnsaminspire_5_be.auth.domain.dto.UserLoginRequestDTO;
import com.inspire.lgcnsaminspire_5_be.auth.domain.dto.UserRequestDTO;
import com.inspire.lgcnsaminspire_5_be.auth.domain.dto.UserResponseDTO;
import com.inspire.lgcnsaminspire_5_be.auth.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder; // 회원가입 시 암호를 해싱처리하기 위해서 (SecurityConfig)

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserRequestDTO request) {
        System.out.println(">>>> debug user controller signUp ");
        System.out.println(">>>> debug params : " + request);

        // 패스워드 해싱 작업(spring - security)
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        boolean isSuccess = userService.signUp(request);

        Map<String, Object> responseBody = new HashMap<>();

        if (isSuccess) {
            responseBody.put("status", "success");
            responseBody.put("message", "회원가입이 완료되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody); // 201 Created
        } else {
            responseBody.put("status", "fail");
            responseBody.put("message", "이미 존재하는 아이디입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody); // 400 Bad Request
        }

    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> signIn(@Valid @RequestBody UserLoginRequestDTO request) {
        System.out.println(">>>> debug user controller signIn ");
        System.out.println(">>>> debug params : " + request);

        UserResponseDTO userResponse = userService.signIn(request);

        // 토큰 값 앞에 "Bearer " 접두사 결합
        if (userResponse != null && userResponse.getToken() != null) {
            userResponse.setToken("Bearer " + userResponse.getToken());
            System.out.println(">>>> debug user login at :  " + userResponse.getToken());
        }

        // 명세서 양식 규격 패킹
        // {"status": "success", "data": { userId, name, token }}
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "success");
        responseBody.put("data", userResponse);

        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> signOut(@RequestHeader("Authorization") String authorization) {
        System.out.println(">>>> debug user logout controller");
        System.out.println(">>>> debug user controller logout Authorization : " + authorization);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String at = authorization.replace("Bearer ", "");
            userService.signOut(at);
        }

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "success");
        responseBody.put("message", "로그아웃이 완료되었습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}
