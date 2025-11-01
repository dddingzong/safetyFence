package com.project.safetyFence.geofence;

import com.project.safetyFence.geofence.domain.Geofence;
import com.project.safetyFence.geofence.handler.GeofenceEntryHandler;
import com.project.safetyFence.geofence.service.InitialGeofenceCreator;
import com.project.safetyFence.user.domain.User;
import com.project.safetyFence.user.domain.UserAddress;
import com.project.safetyFence.geofence.dto.GeofenceRequestDto;
import com.project.safetyFence.geofence.GeofenceRepository;
import com.project.safetyFence.user.UserRepository;
import com.project.safetyFence.common.service.geocoding.GeocodingService;
import com.project.safetyFence.common.service.geocoding.dto.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceService implements InitialGeofenceCreator {

    private final GeofenceRepository geofenceRepository;
    private final GeocodingService geocodingService;
    private final UserRepository userRepository;
    private final List<GeofenceEntryHandler> entryHandlers; // 핸들러 리스트 주입

    @Override
    public Geofence createHomeGeofence(UserAddress userAddress) {
        String address = userAddress.getHomeStreetAddress();
        Coordinate coordinate = geocodingService.convertAddressToCoordinate(address);

        Geofence geofence = new Geofence(
                userAddress.getUser(),
                "집",
                address,
                coordinate.getLatitude(),
                coordinate.getLongitude(),
                0,
                100
        );
        return geofence;
    }

    @Override
    public Geofence createCenterGeofence(UserAddress userAddress) {
        String address = userAddress.getCenterStreetAddress();
        Coordinate coordinate = geocodingService.convertAddressToCoordinate(address);

        Geofence geofence = new Geofence(
                userAddress.getUser(),
                "센터",
                address,
                coordinate.getLatitude(),
                coordinate.getLongitude(),
                0,
                100
        );
        return geofence;
    }

    @Transactional(readOnly = true)
    public List<Geofence> findGeofenceByNumber(String userNumber) {
        User user = userRepository.findByNumber(userNumber);
        List<Geofence> geofences = geofenceRepository.findByUser(user);
        return geofences;
    }

    @Transactional
    public void userFenceIn(String userNumber, Long geofenceId) {
        User user = userRepository.findByNumberWithGeofences(userNumber);

        Geofence geofence = user.getGeofences().stream()
                .filter(g -> g.getId().equals(geofenceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Geofence not found"));

        // 전략 패턴 적용: 타입에 맞는 핸들러 찾아서 실행
        GeofenceEntryHandler handler = entryHandlers.stream()
                .filter(h -> h.supports(geofence.getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported geofence type: " + geofence.getType()));

        handler.handle(user, geofence);
    }

    @Transactional
    public void createNewFence(String userNumber, GeofenceRequestDto geofenceRequestDto) {
        User user = userRepository.findByNumber(userNumber);
        String address = geofenceRequestDto.getAddress();
        Coordinate coordinate = geocodingService.convertAddressToCoordinate(address);

        Geofence geofence;
        if (geofenceRequestDto.getType() == 0) { // 영구 지오펜스
            geofence = new Geofence(
                    user,
                    geofenceRequestDto.getName(),
                    address,
                    coordinate.getLatitude(),
                    coordinate.getLongitude(),
                    0,
                    999
            );
        } else { // 일시 지오펜스
            geofence = new Geofence(
                    user,
                    geofenceRequestDto.getName(),
                    address,
                    coordinate.getLatitude(),
                    coordinate.getLongitude(),
                    1,
                    java.time.LocalDateTime.parse(geofenceRequestDto.getStartTime()),
                    java.time.LocalDateTime.parse(geofenceRequestDto.getEndTime()),
                    100
            );
        }

        user.addGeofence(geofence);
        geofenceRepository.save(geofence);
    }

    @Transactional
    public void deleteFence(String userNumber, Long geofenceId) {
        User user = userRepository.findByNumberWithGeofences(userNumber);

        Geofence geofenceToDelete = user.getGeofences().stream()
                .filter(geofence -> geofence.getId().equals(geofenceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Geofence not found"));

        user.removeGeofence(geofenceToDelete);
        // orphanRemoval = true로 트랜잭션 종료 시 자동 삭제
    }

}
