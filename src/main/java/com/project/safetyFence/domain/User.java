package com.project.safetyFence.domain;

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


    // 사용자 신규 가입 시
    public User (String name, String password, LocalDate birth, String number, String homeAddress, String centerAddress, String linkCode) {
        this.name = name;
        this.password = password;
        this.birth = birth;
        this.number = number;
        this.homeAddress = homeAddress;
        this.centerAddress = centerAddress;
        this.linkCode = linkCode;
    }

    public void updateHomeAddress(String homeAddress){
        this.homeAddress = homeAddress;
    }

    public void updateCenterAddress(String centerAddress){
        this.centerAddress = centerAddress;
    }

}
