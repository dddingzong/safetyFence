package com.project.safetyFence.service;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserAddress;
import com.project.safetyFence.domain.dto.SignUpRequestDto;
import com.project.safetyFence.generator.LinkCodeGenerator;
import com.project.safetyFence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserService {

    private final LinkCodeGenerator linkCodeGenerator;
    private final UserRepository userRepository;
    private final UserAddressService userAddressService;

    public boolean checkExistNumber(String number) {
        return userRepository.existsByNumber(number);
    }

    public User findByNumber(String number) {
        return userRepository.findByNumber(number);
    }

    public boolean checkDuplicateNumber (String number) {
        return userRepository.existsByNumber(number);
    }

    @Transactional
    public User registerUser(SignUpRequestDto signUpRequestDto) {
        // user 저장
        User user = makeUserEntity(signUpRequestDto);
        // userAddress 저장
        UserAddress userAddress = userAddressService.makeUserAddressEntity(signUpRequestDto, user);
        user.addUserAddress(userAddress);

        userRepository.save(user);

        // user 주소로 geofence 생성
        //saveGeofenceService.saveInitialGeofence(user, userAddress);

        return user;
    }


    public User makeUserEntity(SignUpRequestDto signUpRequestDto) {
        // linkCode 생성
        String linkCode = linkCodeGenerator.generate();

        // linkCode 중복 검사
        String uniqueLinkCode = checkLinkCodeDuplicate(linkCode);

        String number = signUpRequestDto.getNumber();
        String name = signUpRequestDto.getName();
        String password = signUpRequestDto.getPassword();
        String birthString = signUpRequestDto.getBirth();

        // DTO의 String -> Java의 LocalDate로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(birthString, formatter); // 여기서 변환

        User user = new User(number, name, password, birthDate, uniqueLinkCode);

        return user;
    }

    private String checkLinkCodeDuplicate(String firstLinkCode){
        String linkCode = firstLinkCode;
        while (userRepository.existsByLinkCode(linkCode)){
            linkCode = linkCodeGenerator.generate();
        }
        return linkCode;
    }

}
