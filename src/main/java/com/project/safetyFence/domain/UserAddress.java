package com.project.safetyFence.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class UserAddress {

    @Id
    private String number;

    @Version
    private Long version;

    // 1:1 양방향 관계의 주인 (FK 관리)
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // number를 User의 PK와 공유
    @JoinColumn(name = "number")
    private User user;

    @Column(nullable = false)
    private String homeAddress;

    private String centerAddress;

    @Column(nullable = false)
    private String homeStreetAddress;

    @Column(nullable = false)
    private String homeStreetAddressDetail;

    private String centerStreetAddress;

    // User 객체를 받는 생성자 (권장)
    public UserAddress(User user, String homeAddress, String centerAddress,
                       String homeStreetAddress, String homeStreetAddressDetail,
                       String centerStreetAddress) {
        this.user = user;
        this.number = user.getNumber();
        this.homeAddress = homeAddress;
        this.centerAddress = centerAddress;
        this.homeStreetAddress = homeStreetAddress;
        this.homeStreetAddressDetail = homeStreetAddressDetail;
        this.centerStreetAddress = centerStreetAddress;
    }

    public void updateHomeStreetAddress(String homeAddress, String homeStreetAddress, String homeStreetAddressDetail) {
        this.homeAddress = homeAddress;
        this.homeStreetAddress = homeStreetAddress;
        this.homeStreetAddressDetail = homeStreetAddressDetail;
    }

    public void updateCenterStreetAddress(String centerAddress, String centerStreetAddress) {
        this.centerAddress = centerAddress;
        this.centerStreetAddress = centerStreetAddress;
    }

    // 연관관계 편의 메서드 (package-private)
    void registerUser(User user) {
        this.user = user;
        this.number = user.getNumber();
    }
}
