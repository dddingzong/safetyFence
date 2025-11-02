package com.project.safetyFence.geofence.domain;
import com.project.safetyFence.user.domain.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Geofence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 양방향 관계의 주인 (FK 관리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(nullable = false)
    private int type; // 0: 영구, 1: 일시

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int maxSequence;

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    // 영구적인 지오펜스 생성자
    public Geofence(User user, String name, String address, BigDecimal latitude, BigDecimal longitude, int type, int maxSequence) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.location = createPoint(latitude.doubleValue(), longitude.doubleValue());
        this.type = type;
        this.maxSequence = maxSequence;
    }

    // 일시적인 지오펜스 생성자
    public Geofence(User user, String name, String address, BigDecimal latitude, BigDecimal longitude, int type, LocalDateTime startTime, LocalDateTime endTime, int maxSequence) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.location = createPoint(latitude.doubleValue(), longitude.doubleValue());
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxSequence = maxSequence;
    }

    // Point 생성 헬퍼 메서드
    private Point createPoint(double latitude, double longitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326); // WGS84 좌표계
        return point;
    }

    // 편의 메서드: 위도 조회
    public double getLatitude() {
        return location.getY();
    }

    // 편의 메서드: 경도 조회
    public double getLongitude() {
        return location.getX();
    }

    // 연관관계 편의 메서드
    public void registerUser(User user) {
        this.user = user;
    }

    public void decreaseMaxSequence() {
        this.maxSequence--;
    }

}
