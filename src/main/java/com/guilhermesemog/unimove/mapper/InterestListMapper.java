package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.interestlist.InterestListCreate;
import com.guilhermesemog.unimove.dto.interestlist.InterestListPatch;
import com.guilhermesemog.unimove.dto.interestlist.InterestListResponse;
import com.guilhermesemog.unimove.dto.interestlist.InterestListUpdate;
import com.guilhermesemog.unimove.model.InterestList;
import org.springframework.stereotype.Component;


@Component
public class InterestListMapper {
    public InterestList toEntity(InterestListCreate body) {
        if (body == null) return new InterestList();

        return new InterestList(
                body.referenceDate(),
                body.closingDate()
        );
    }

    public InterestListResponse toResponse(InterestList interestList) {
        return new InterestListResponse(
                interestList.getId(),
                interestList.getReferenceDate(),
                interestList.getClosingDate(),
                interestList.getListStatus()
        );
    }

    public InterestList update(InterestListUpdate newInterestList, InterestList interestList) {
        interestList.setReferenceDate(newInterestList.referenceDate());
        interestList.setClosingDate(newInterestList.closingDate());
        interestList.setListStatus(newInterestList.listStatus());
        return interestList;
    }

    public InterestList update(InterestListPatch newInterestList, InterestList interestList) {
        if (newInterestList.referenceDate() != null) {
            interestList.setReferenceDate(newInterestList.referenceDate());
        }

        if (newInterestList.closingDate() != null) {
            interestList.setClosingDate(newInterestList.closingDate());
        }

        if (newInterestList.listStatus() != null) {
            interestList.setListStatus(newInterestList.listStatus());
        }
        return interestList;
    }
}
