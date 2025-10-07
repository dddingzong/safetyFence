package com.project.paypass_renewal.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class DetailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long logId;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private LocalDateTime fenceInTime;

    private LocalDateTime fenceOutTime;

    @Column(nullable = false)
    private Long stationNumber;

    private String routeIdList;

    public DetailLog(Long logId, String number, LocalDateTime fenceInTime, LocalDateTime fenceOutTime, Long stationNumber, String routeIdList) {
        this.logId = logId;
        this.number = number;
        this.fenceInTime = fenceInTime;
        this.fenceOutTime = fenceOutTime;
        this.stationNumber = stationNumber;
        this.routeIdList = routeIdList;
    }

    // 테스트용 생성자
    public DetailLog(Long id, Long logId, String number, LocalDateTime fenceInTime, LocalDateTime fenceOutTime, Long stationNumber, String routeIdList) {
        this.id = id;
        this.logId = logId;
        this.number = number;
        this.fenceInTime = fenceInTime;
        this.fenceOutTime = fenceOutTime;
        this.stationNumber = stationNumber;
        this.routeIdList = routeIdList;
    }
}
