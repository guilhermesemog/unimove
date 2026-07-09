package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.enums.ListStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "interest_lists")
public class InterestList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime referenceDate = LocalDateTime.now();

    @Column
    private LocalDateTime closingDate = LocalDateTime.now().plusDays(1);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListStatus listStatus = ListStatus.OPEN;

    public InterestList(LocalDateTime referenceDate, LocalDateTime closingDate, ListStatus listStatus) {
        this.referenceDate = referenceDate;
        this.closingDate = closingDate;
        this.listStatus = listStatus;
    }

    public InterestList(LocalDateTime referenceDate, LocalDateTime closingDate) {
        this.referenceDate = referenceDate;
        this.closingDate = closingDate;
    }
}
