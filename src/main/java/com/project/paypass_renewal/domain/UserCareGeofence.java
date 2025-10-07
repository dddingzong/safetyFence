package com.project.paypass_renewal.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Getter
@Entity
@NoArgsConstructor
public class UserCareGeofence {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal homeLatitude;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal homeLongitude;

    @Column(precision = 38, scale = 8)
    private BigDecimal centerLatitude;

    @Column(precision = 38, scale = 8)
    private BigDecimal centerLongitude;

    public UserCareGeofence(String number, BigDecimal homeLatitude, BigDecimal homeLongitude, BigDecimal centerLatitude, BigDecimal centerLongitude) {
        this.number = number;
        this.homeLatitude = homeLatitude;
        this.homeLongitude = homeLongitude;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
    }
}
