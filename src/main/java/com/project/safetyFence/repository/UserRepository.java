package com.project.safetyFence.repository;

import com.project.safetyFence.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String>{
    boolean existsByNumber(String number);
    User findByNumber(String number);
}
