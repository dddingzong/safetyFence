package com.project.safetyFence.service;

import com.project.safetyFence.domain.Link;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.Wallet;
import com.project.safetyFence.domain.dto.request.NumberRequestDto;
import com.project.safetyFence.domain.dto.request.UserRequestDto;
import com.project.safetyFence.domain.dto.response.PendingListResponseDto;
import com.project.safetyFence.domain.dto.response.WalletResponseDto;
import com.project.safetyFence.repository.LinkRepository;
import com.project.safetyFence.repository.UserRepository;
import com.project.safetyFence.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;

    // 지갑 생성
    public Wallet createWallet(UserRequestDto userRequestDto) {
        String number = userRequestDto.getNumber();
        Wallet wallet = new Wallet(number);
        return walletRepository.save(wallet);
    }

    // 지갑 조회
    public WalletResponseDto getWalletByNumber(NumberRequestDto numberRequestDto) {
        log.info("사용자의 지갑을 조회합니다. 사용자 번호: {}", numberRequestDto.getNumber());

        String number = numberRequestDto.getNumber();

        Wallet wallet = walletRepository.findByNumber(number);

        int balance = wallet.getBalance();
        int pendingAmount = wallet.getPendingAmount();

        WalletResponseDto walletResponseDto = new WalletResponseDto(number, balance, pendingAmount);
        return walletResponseDto;
    }

    public List<PendingListResponseDto> getUsersPendingList(NumberRequestDto numberRequestDto) {
        ArrayList<PendingListResponseDto> pendingList = new ArrayList<>();
        log.info("이용자의 지갑에 연결된 지갑 목록을 조회합니다. 이용자 번호: {}", numberRequestDto.getNumber());

        String number = numberRequestDto.getNumber();

        List<Link> linkList = linkRepository.findBySupporterNumber(number);

        log.info("이용자 번호: {}의 지갑에 연결된 지갑 수: {}", number, linkList.size());

        for (Link link : linkList) {
            String userNumber = link.getUserNumber();
            String relation = link.getRelation();

            Wallet wallet = walletRepository.findByNumber(userNumber);
            int balance = wallet.getBalance();
            int pendingAmount = wallet.getPendingAmount();

            User user = userRepository.findByNumber(userNumber);
            String name = user.getName();

            PendingListResponseDto pendingListResponseDto = new PendingListResponseDto(
                    userNumber, name, relation, balance, pendingAmount
            );

            pendingList.add(pendingListResponseDto);
        }

        return pendingList;
    }
}
