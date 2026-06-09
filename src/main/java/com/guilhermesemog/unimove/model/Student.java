package com.guilhermesemog.unimove.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "students")
public class Student {

    @Id
    private Long id;

    @OneToOne
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
}
