package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByNumber(String number);

}
