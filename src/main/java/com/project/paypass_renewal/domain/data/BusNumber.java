package com.project.paypass_renewal.domain.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class BusNumber {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String routeId;
    private String busName;

    public BusNumber(String routeId, String busName) {
        this.routeId = routeId;
        this.busName = busName;
    }
}
