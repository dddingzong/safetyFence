package com.project.paypass_renewal.domain.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long stationNumber;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false, precision = 38, scale = 8)
    private BigDecimal latitude;

    @Column(columnDefinition = "TEXT")
    private String busInfo;

    public Station(String name, Long stationNumber, BigDecimal longitude, BigDecimal latitude, String busInfo) {
        this.name = name;
        this.stationNumber = stationNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.busInfo = busInfo;
    }
}
