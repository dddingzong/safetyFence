package com.project.safetyFence.link.domain;
import com.project.safetyFence.user.domain.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Link {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 양방향 관계의 주인 (FK 관리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", nullable = false)
    private User user;

    @Column(nullable = false)
    private String userNumber;

    private String relation;

    public Link(User user, String userNumber, String relation) {
        this.user = user;
        this.userNumber = userNumber;
        this.relation = relation;
    }

    // 연관관계 편의 메서드
    public void registerUser(User user) {
        this.user = user;
    }
}
