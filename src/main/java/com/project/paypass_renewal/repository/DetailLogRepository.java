package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.DetailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetailLogRepository extends JpaRepository<DetailLog, Long> {
    List<DetailLog> findByLogId(Long logId);
}
