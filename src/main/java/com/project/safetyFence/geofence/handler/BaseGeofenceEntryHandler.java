package com.project.safetyFence.geofence.handler;

import com.project.safetyFence.geofence.domain.Geofence;
import com.project.safetyFence.log.domain.Log;
import com.project.safetyFence.user.domain.User;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public abstract class BaseGeofenceEntryHandler implements GeofenceEntryHandler {

    @Override
    public final void handle(User user, Geofence geofence) {
        // 1. 로그 저장 (공통 로직)
        saveEntryLog(user, geofence);

        // 2. 타입별 처리 (하위 클래스 구현)
        handleEntry(user, geofence);
    }
    
    private void saveEntryLog(User user, Geofence geofence) {
        Log entryLog = new Log(
            user,
            geofence.getName(),
            geofence.getAddress(),
            LocalDateTime.now()
        );

        // 양방향 관계 동기화 (cascade로 자동 저장)
        user.addLog(entryLog);

        log.info("지오펜스 진입 로그 저장: 사용자={}, 지오펜스={}, 시간={}",
                user.getNumber(), geofence.getName(), entryLog.getArriveTime());
    }
    
    protected abstract void handleEntry(User user, Geofence geofence);
}