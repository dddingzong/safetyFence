package com.project.safetyFence.service;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean checkExistNumber(String number) {
        return userRepository.existsByNumber(number);
    }

    public User findByNumber(String number) {
        return userRepository.findByNumber(number);
    }
}
