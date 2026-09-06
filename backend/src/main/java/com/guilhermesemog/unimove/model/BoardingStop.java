package com.guilhermesemog.unimove.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "boarding_stops")
public class BoardingStop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String local;

    public BoardingStop(String local) {
        this.local = local;
    }
}
