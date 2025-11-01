package com.project.safetyFence.location.domain;
import com.project.safetyFence.user.domain.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class UserLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime savedTime;

    // N:1 양방향 관계의 주인 (FK 관리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", nullable = false)
    private User user;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal longitude;

    // User 객체를 받는 생성자 (권장)
    public UserLocation(User user, BigDecimal latitude, BigDecimal longitude) {
        this.user = user;
        this.savedTime = LocalDateTime.now();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // 연관관계 편의 메서드
    public void registerUser(User user) {
        this.user = user;
    }
}
