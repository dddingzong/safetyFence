package com.project.safetyFence.repository;

import com.project.safetyFence.domain.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
}
