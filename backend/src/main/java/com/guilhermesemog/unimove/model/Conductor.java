package com.guilhermesemog.unimove.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@Table(name = "conductors")
public class Conductor {

    @Id
    private Long id;

    @MapsId
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private String license;

    @Column(nullable = false)
    private LocalDate licenseExpirationDate;

    public Conductor(User user, String license, LocalDate licenseExpirationDate) {
        this.user = user;
        this.license = license;
        this.licenseExpirationDate = licenseExpirationDate;
    }
}
