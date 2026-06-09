package com.guilhermesemog.unimove.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "universities")
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String address;
}
