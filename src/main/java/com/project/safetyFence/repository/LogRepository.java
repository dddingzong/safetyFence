package com.project.safetyFence.repository;

import com.project.safetyFence.domain.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findByNumberOrderByDepartureTimeDesc(String number);

}
