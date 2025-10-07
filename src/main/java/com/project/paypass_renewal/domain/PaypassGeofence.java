package com.project.paypass_renewal.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class PaypassGeofence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private LocalDateTime fenceInTime;

    private LocalDateTime fenceOutTime;

    @Column(nullable = false)
    private Long stationNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String busInfo;

    public String toString() {
        return "PaypassGeofence{" +
                "fenceInTime=" + fenceInTime +
                ", fenceOutTime=" + fenceOutTime +
                ", stationNumber=" + stationNumber +
                ", busInfo='" + busInfo + '\'' +
                '}';
    }

    public PaypassGeofence(String number, Long stationNumber, String busInfo) {
        this.number = number;
        this.fenceInTime = LocalDateTime.now();
        this.fenceOutTime = null;
        this.stationNumber = stationNumber;
        this.busInfo = busInfo;
    }

    public PaypassGeofence(String number, Long stationNumber, String busInfo, LocalDateTime fenceOutTime) {
        this.number = number;
        this.fenceInTime = fenceOutTime.minusMinutes(2);
        this.fenceOutTime = fenceOutTime;
        this.stationNumber = stationNumber;
        this.busInfo = busInfo;
    }

    // 테스트용
    public PaypassGeofence(Long id, String number, Long stationNumber, String busInfo, LocalDateTime fenceOutTime) {
        this.id = id;
        this.number = number;
        this.fenceInTime = fenceOutTime.minusMinutes(2);
        this.fenceOutTime = fenceOutTime;
        this.stationNumber = stationNumber;
        this.busInfo = busInfo;
    }


    public void userFenceOut(){
        this.fenceOutTime = LocalDateTime.now();
    }

    public boolean fenceOutTimeIsNull(){
        return fenceOutTime == null;
    }
}
