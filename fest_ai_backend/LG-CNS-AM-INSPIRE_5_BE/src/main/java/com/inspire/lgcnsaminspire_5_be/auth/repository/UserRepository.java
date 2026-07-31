package com.inspire.lgcnsaminspire_5_be.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inspire.lgcnsaminspire_5_be.auth.domain.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    public Optional<UserEntity> findByloginIdAndPassword(String loginId, String password);

    boolean existsByLoginId(String loginId);

    public Optional<UserEntity> findByLoginId(String loginId);
}
