package com.project.safetyFence.link;

import com.project.safetyFence.link.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<Link, Long> {
    boolean existsByUser_NumberAndUserNumber(String userNumber, String linkedUserNumber);
}
