package com.project.safetyFence.repository;

import com.project.safetyFence.domain.DetailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetailLogRepository extends JpaRepository<DetailLog, Long> {
    List<DetailLog> findByLogId(Long logId);
}
