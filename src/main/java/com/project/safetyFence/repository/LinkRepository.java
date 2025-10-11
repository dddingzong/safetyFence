package com.project.safetyFence.repository;

import com.project.safetyFence.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Long> {

    @Query("SELECT l.userNumber FROM Link l WHERE l.supporterNumber = :supporterNumber")
    List<String> findUserNumbersBySupporterNumber(@Param("supporterNumber") String supporterNumber);

    @Modifying
    int deleteBySupporterNumberAndUserNumber(String supporterNumber, String userNumber);

    boolean existsBySupporterNumberAndUserNumber(String supporterNumber, String userNumber);

    Link findByUserNumberAndSupporterNumber(String supporterNumber, String userNumber );

    List<Link> findBySupporterNumber(String userNumber);

}

