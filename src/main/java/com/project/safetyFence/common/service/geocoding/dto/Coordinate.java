package com.project.safetyFence.common.service.geocoding.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
@AllArgsConstructor
public class Coordinate {
    private final BigDecimal latitude;
    private final BigDecimal longitude;
}
