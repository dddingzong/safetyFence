package com.project.safetyFence.geofence.domain;
import com.project.safetyFence.user.domain.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Geofence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 양방향 관계의 주인 (FK 관리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false)
    private int type; // 0: 영구, 1: 일시

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int maxSequence;

    // 영구적인 지오펜스 생성자
    public Geofence(User user, String name, String address, BigDecimal latitude, BigDecimal longitude, int type, int maxSequence) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.maxSequence = maxSequence;
    }

    // 일시적인 지오펜스 생성자
    public Geofence(User user, String name, String address, BigDecimal latitude, BigDecimal longitude, int type, LocalDateTime startTime, LocalDateTime endTime, int maxSequence) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxSequence = maxSequence;
    }

    // 연관관계 편의 메서드
    public void registerUser(User user) {
        this.user = user;
    }

    public void decreaseMaxSequence() {
        this.maxSequence--;
    }

}
