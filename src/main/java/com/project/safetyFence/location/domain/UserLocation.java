package com.project.safetyFence.location.domain;
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
public class UserLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime savedTime;

    // N:1 양방향 관계의 주인 (FK 관리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    // User 객체를 받는 생성자 (권장)
    public UserLocation(User user, BigDecimal latitude, BigDecimal longitude) {
        this.user = user;
        this.savedTime = LocalDateTime.now();
        this.location = createPoint(latitude.doubleValue(), longitude.doubleValue());
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
}
