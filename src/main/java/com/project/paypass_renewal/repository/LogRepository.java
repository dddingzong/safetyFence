package com.project.paypass_renewal.repository;

import com.project.paypass_renewal.domain.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findByNumberOrderByDepartureTimeDesc(String number);

}
