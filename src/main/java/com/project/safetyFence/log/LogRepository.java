package com.project.safetyFence.log;

import com.project.safetyFence.log.domain.Log;
import com.project.safetyFence.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findByUser(User user);
}
