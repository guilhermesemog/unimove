package com.guilhermesemog.unimove.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "conductors")
public class Conductor {

    @Id
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private String license;

    @Column(nullable = false)
    private LocalDate licenseExpirationDate;
}
