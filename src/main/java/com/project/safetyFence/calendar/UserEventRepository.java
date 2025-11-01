package com.project.safetyFence.calendar;

import com.project.safetyFence.calendar.domain.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    Optional<UserEvent> findById(Long aLong);
}
