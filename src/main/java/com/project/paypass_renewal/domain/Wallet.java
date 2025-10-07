package com.project.paypass_renewal.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Wallet {

    @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String number;
    private int balance;
    private int pendingAmount;

    public Wallet(String number) {
        this.number = number;
        this.balance = 0;
        this.pendingAmount = 0;
    }
}
