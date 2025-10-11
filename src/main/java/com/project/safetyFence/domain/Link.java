package com.project.safetyFence.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Link {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String supporterNumber;

    @Column(nullable = false)
    private String userNumber;

    @Column(nullable = false)
    private String relation;

    public Link(String supporterNumber, String userNumber, String relation) {
        this.supporterNumber = supporterNumber;
        this.userNumber = userNumber;
        this.relation = relation;
    }
}
