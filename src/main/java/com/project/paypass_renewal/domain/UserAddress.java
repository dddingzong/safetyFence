package com.project.paypass_renewal.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Entity
@RequiredArgsConstructor
public class UserAddress {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private String homeStreetAddress;

    @Column(nullable = false)
    private String homeStreetAddressDetail;

    private String centerStreetAddress;

    public UserAddress(String number, String homeStreetAddress, String homeStreetAddressDetail, String centerStreetAddress) {
        this.number = number;
        this.homeStreetAddress = homeStreetAddress;
        this.homeStreetAddressDetail = homeStreetAddressDetail;
        this.centerStreetAddress = centerStreetAddress;
    }

    public void updateHomeStreetAddress(String homeStreetAddress, String homeStreetAddressDetail) {
        this.homeStreetAddress = homeStreetAddress;
        this.homeStreetAddressDetail = homeStreetAddressDetail;
    }

    public void updateCenterStreetAddress(String centerStreetAddress) {
        this.centerStreetAddress = centerStreetAddress;
    }
}
