package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.university.UniversityPatchRequestBody;
import com.guilhermesemog.unimove.dto.university.UniversityPostRequestBody;
import com.guilhermesemog.unimove.dto.university.UniversityPutRequestBody;
import com.guilhermesemog.unimove.dto.university.UniversityResponseBody;
import com.guilhermesemog.unimove.model.University;
import org.springframework.stereotype.Component;

@Component
public class UniversityMapper {

    public University toEntity(UniversityPostRequestBody universityPostRequestBody) {
        return new University(
                universityPostRequestBody.name(),
                universityPostRequestBody.address()
        );
    }

    public UniversityResponseBody toResponseBody(University university) {
        return new UniversityResponseBody(
                university.getId(),
                university.getName(),
                university.getAddress()
        );
    }

    public University updateUniversity(UniversityPutRequestBody newUniversity, University university) {
        university.setName(newUniversity.name());
        university.setAddress(newUniversity.address());

        return university;
    }

    public University updateUniversity(UniversityPatchRequestBody newUniversity, University university) {

        if (newUniversity.name() != null) {
            university.setName(newUniversity.name());
        }

        if (newUniversity.address() != null) {
            university.setAddress(newUniversity.address());
        }

        return university;
    }
}