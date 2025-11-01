package com.project.safetyFence.common.service.geocoding;

import com.project.safetyFence.common.service.geocoding.dto.Coordinate;

public interface GeocodingService {
    Coordinate convertAddressToCoordinate(String address);
}
