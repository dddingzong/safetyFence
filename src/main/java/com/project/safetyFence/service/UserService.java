package com.project.safetyFence.service;

import com.project.safetyFence.domain.ServiceCode;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.dto.request.LoginRequestDto;
import com.project.safetyFence.domain.dto.request.UserRequestDto;
import com.project.safetyFence.generator.LinkCodeGenerator;
import com.project.safetyFence.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAddressService userAddressService;
    private final LinkCodeGenerator linkCodeGenerator;
    private final UserCareGeofenceService userCareGeofenceService;
    private final WalletService walletService;

    public boolean checkDuplicateNumber (String number) {
        return userRepository.existsByNumber(number);
    }

    @Transactional
    public User saveNewUser(UserRequestDto userRequestDto) {
        // linkCode 생성
        String linkCode = linkCodeGenerator.generate();

        // linkCode 중복 검사
        String uniqueLinkCode = checkLinkCodeDuplicate(linkCode);

        // User 생성 및 저장
        User user = toEntity(userRequestDto, uniqueLinkCode);

        // User 생성 몇 저장
        userRepository.save(user);

        // UserAddress 생성 및 저장
        userAddressService.saveNewUserAddress(userRequestDto);

        // UserCareGeofence 생성 및 저장
        userCareGeofenceService.saveUserGeofence(userRequestDto);

        // Wallet 생성 및 저장
        walletService.createWallet(userRequestDto);

        return user;
    }


    public boolean checkExistNumber(String number) {
        return userRepository.existsByNumber(number);
    }

    public boolean matchPassword(LoginRequestDto loginRequestDto) {

        String number = loginRequestDto.getNumber();
        String password = loginRequestDto.getPassword();

        User user = userRepository.findByNumber(number);

        return user.getPassword().equals(password);

    }


    private String checkLinkCodeDuplicate(String firstLinkCode){
        String linkCode = firstLinkCode;
        while (userRepository.existsByLinkCode(linkCode)){
            linkCode = linkCodeGenerator.generate();
        }
        return linkCode;
    }

    private User toEntity(UserRequestDto userRequestDto, String linkCode){
        String name = userRequestDto.getName();
        String password = userRequestDto.getPassword();
        LocalDate birth = userRequestDto.getBirth();
        String number = userRequestDto.getNumber();
        String homeAddress = userRequestDto.getHomeAddress();
        String centerAddress = userRequestDto.getCenterAddress();
        ServiceCode serviceCode = userRequestDto.getServiceCode();

        User user = new User(name, password, birth, number, homeAddress, centerAddress, linkCode, serviceCode);

        return user;
    }

}
