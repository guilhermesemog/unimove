package com.guilhermesemog.unimove.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String plate;

    @Column(nullable = false)
    private Integer capacity;

    public Vehicle(String plate, Integer capacity) {
        this.plate = plate;
        this.capacity = capacity;
    }
}
