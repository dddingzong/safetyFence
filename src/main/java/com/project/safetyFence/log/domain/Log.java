package com.project.safetyFence.log.domain;
import com.project.safetyFence.user.domain.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class Log {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String locationAddress;

    @Column(nullable = false)
    private LocalDateTime arriveTime;

    // N:1 양방향 관계의 주인 (FK 관리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", nullable = false)
    private User user;

    public Log(User user, String location, String locationAddress, LocalDateTime arriveTime) {
        this.user = user;
        this.location = location;
        this.locationAddress = locationAddress;
        this.arriveTime = arriveTime;
    }

    // 연관관계 편의 메서드
    public void registerUser(User user) {
        this.user = user;
    }
}
