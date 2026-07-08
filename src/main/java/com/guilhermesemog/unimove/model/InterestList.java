package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.ListStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "interest_lists")
public class InterestList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime referenceDate;

    @Column
    private LocalDateTime closingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListStatus listStatus = ListStatus.OPEN;
}
