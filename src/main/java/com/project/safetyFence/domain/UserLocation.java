package com.project.safetyFence.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class UserLocation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime savedTime;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal longitude;

    public UserLocation(String number, BigDecimal latitude, BigDecimal longitude) {
        this.number = number;
        this.savedTime = LocalDateTime.now();
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
