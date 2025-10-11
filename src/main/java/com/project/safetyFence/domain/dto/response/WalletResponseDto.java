package com.project.safetyFence.domain.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WalletResponseDto {
    private String number;
    private int balance;
    private int pendingAmount;

    public WalletResponseDto(String number, int balance, int pendingAmount) {
        this.number = number;
        this.balance = balance;
        this.pendingAmount = pendingAmount;
    }
}
