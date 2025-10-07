package com.project.paypass_renewal.domain.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PendingListResponseDto {

    private String number;
    private String name;
    private String relation;
    private int balance;
    private int pendingAmount;

    public PendingListResponseDto(String number, String name, String relation, int balance, int pendingAmount) {
        this.number = number;
        this.name = name;
        this.relation = relation;
        this.balance = balance;
        this.pendingAmount = pendingAmount;
    }

}
