package com.project.safetyFence.calendar.domain;
import com.project.safetyFence.user.domain.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
public class UserEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 양방향 관계의 주인 (FK 관리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String event;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column
    private LocalTime startTime;

    public UserEvent(User user, String event, LocalDate eventDate, LocalTime startTime) {
        this.user = user;
        this.event = event;
        this.eventDate = eventDate;
        this.startTime = startTime;
    }

    // 연관관계 편의 메서드
    public void registerUser(User user) {
        this.user = user;
    }

}
