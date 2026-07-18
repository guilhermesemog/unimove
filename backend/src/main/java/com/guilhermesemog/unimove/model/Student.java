package com.guilhermesemog.unimove.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "students")
public class Student {

    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private Long period;

    @Column(nullable = false)
    private String course;

    @Column(nullable = false)
    private String address;

    @ManyToOne
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne
    @JoinColumn(name = "preferred_boarding_stop_id")
    private BoardingStop preferredBoardingStop;

    public Student(User user, Long period, String course, String address, University university, BoardingStop preferredBoardingStop) {
        this.user = user;
        this.period = period;
        this.course = course;
        this.address = address;
        this.university = university;
        this.preferredBoardingStop = preferredBoardingStop;
    }
}
