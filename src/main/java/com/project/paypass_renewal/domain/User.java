package com.project.paypass_renewal.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "\"user\"")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private String homeAddress;

    private String centerAddress;

    @Column(nullable = false)
    private String linkCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceCode serviceCode;

    // 사용자 신규 가입 시
    public User (String name, String password, LocalDate birth, String number, String homeAddress, String centerAddress, String linkCode, ServiceCode serviceCode) {
        this.name = name;
        this.password = password;
        this.birth = birth;
        this.number = number;
        this.homeAddress = homeAddress;
        this.centerAddress = centerAddress;
        this.linkCode = linkCode;
        this.serviceCode = serviceCode;
    }

    public void updateHomeAddress(String homeAddress){
        this.homeAddress = homeAddress;
    }

    public void updateCenterAddress(String centerAddress){
        this.centerAddress = centerAddress;
    }

}
