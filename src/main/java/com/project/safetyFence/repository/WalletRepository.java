package com.project.safetyFence.repository;

import com.project.safetyFence.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByNumber(String number);

}
