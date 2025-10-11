package com.project.safetyFence.repository;

import com.project.safetyFence.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLinkCode(String linkCode);

    boolean existsByNumber(String number);

    List<User> findByNumberIn(List<String> userNumbers);

    User findByNumber(String number);

    User findByLinkCode(String linkCode);

}
