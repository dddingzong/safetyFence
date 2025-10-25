package com.project.safetyFence.repository;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    /**
     * 특정 사용자의 가장 최근 위치 조회
     * @param user 사용자
     * @return 최근 위치 (Optional)
     */
    @Query("SELECT ul FROM UserLocation ul WHERE ul.user = :user ORDER BY ul.savedTime DESC LIMIT 1")
    Optional<UserLocation> findLatestByUser(@Param("user") User user);
}
