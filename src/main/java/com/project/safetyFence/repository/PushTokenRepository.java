package com.project.safetyFence.repository;


import com.project.safetyFence.domain.token.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    Optional<PushToken> findByNumber(String number);
}