package com.project.paypass_renewal.domain.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor
public class BusTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String routeId;

    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private String arrivalTime;

    public BusTime(String routeId, int sequence, String arrivalTime) {
        this.routeId = routeId;
        this.sequence = sequence;
        this.arrivalTime = arrivalTime;
    }
}
