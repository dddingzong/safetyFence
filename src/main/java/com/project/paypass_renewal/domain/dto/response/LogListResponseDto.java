package com.project.paypass_renewal.domain.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class LogListResponseDto {

    private Long id;
    private String number;
    private String name;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String departureLocation;
    private String arrivalLocation;

    public LogListResponseDto(Long id, String number, String name, LocalDateTime departureTime, LocalDateTime arrivalTime, String departureLocation, String arrivalLocation) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.departureLocation = departureLocation;
        this.arrivalLocation = arrivalLocation;
    }
}
