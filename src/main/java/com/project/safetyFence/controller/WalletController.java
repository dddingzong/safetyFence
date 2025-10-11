package com.project.safetyFence.controller;

import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.response.PendingListResponseDto;
import com.project.safetyFence.domain.dto.response.WalletResponseDto;
import com.project.safetyFence.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/wallet/getWalletInfo")
    public ResponseEntity<WalletResponseDto> getWalletInfo(@RequestBody NumberRequestDto numberRequestDto) {
        WalletResponseDto walletByNumber = walletService.getWalletByNumber(numberRequestDto);
        return ResponseEntity.ok(walletByNumber);
    }

    @PostMapping("/wallet/getUsersPendingList")
    public ResponseEntity<List<PendingListResponseDto>> getUsersPendingList(@RequestBody NumberRequestDto numberRequestDto) {
        List<PendingListResponseDto> pendingList = walletService.getUsersPendingList(numberRequestDto);
        return ResponseEntity.ok(pendingList);
    }



}
