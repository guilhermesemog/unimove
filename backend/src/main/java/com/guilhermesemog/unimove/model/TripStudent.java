package com.guilhermesemog.unimove.model;

import com.guilhermesemog.unimove.model.embeddable.TripStudentId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "trip_students")
public class TripStudent {

    @EmbeddedId
    private TripStudentId id;

    @MapsId("tripId")
    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @MapsId("studentId")
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public TripStudent(Trip trip, Student student) {
        this.id = new TripStudentId(trip.getId(), student.getId());
        this.trip = trip;
        this.student = student;
    }
}
