package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.university.UniversityPatch;
import com.guilhermesemog.unimove.dto.university.UniversityCreate;
import com.guilhermesemog.unimove.dto.university.UniversityUpdate;
import com.guilhermesemog.unimove.dto.university.UniversityResponse;
import com.guilhermesemog.unimove.model.University;
import org.springframework.stereotype.Component;

@Component
public class UniversityMapper {

    public University toEntity(UniversityCreate body) {
        return new University(
                body.name(),
                body.address()
        );
    }

    public UniversityResponse toResponse(University university) {
        return new UniversityResponse(
                university.getId(),
                university.getName(),
                university.getAddress()
        );
    }

    public University updateUniversity(UniversityUpdate newUniversity, University university) {
        university.setName(newUniversity.name());
        university.setAddress(newUniversity.address());

        return university;
    }

    public University updateUniversity(UniversityPatch newUniversity, University university) {

        if (newUniversity.name() != null) {
            university.setName(newUniversity.name());
        }

        if (newUniversity.address() != null) {
            university.setAddress(newUniversity.address());
        }

        return university;
    }
}