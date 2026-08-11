package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.interestlist.InterestListCreate;
import com.guilhermesemog.unimove.dto.interestlist.InterestListPatch;
import com.guilhermesemog.unimove.dto.interestlist.InterestListResponse;
import com.guilhermesemog.unimove.dto.interestlist.InterestListUpdate;
import com.guilhermesemog.unimove.dto.university.UniversityResponse;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.University;
import org.springframework.stereotype.Component;


@Component
public class InterestListMapper {

    UniversityMapper universityMapper;

    public InterestListMapper(UniversityMapper universityMapper) {
        this.universityMapper = universityMapper;
    }

    public InterestList toEntity(InterestListCreate body, University destination) {
        return new InterestList(
                body.referenceDate(),
                body.closingTime(),
                body.departureTime(),
                body.arrivalTime(),
                body.returnDepartureTime(),
                body.returnArrivalTime(),
                destination
        );
    }

    public InterestListResponse toResponse(InterestList interestList) {
        UniversityResponse destinationResponse = universityMapper.toResponse(interestList.getDestination());

        return new InterestListResponse(
                interestList.getId(),
                interestList.getReferenceDate(),
                interestList.getClosingTime(),
                interestList.getDepartureTime(),
                interestList.getArrivalTime(),
                interestList.getReturnDepartureTime(),
                interestList.getReturnArrivalTime(),
                destinationResponse,
                interestList.getListStatus()
        );
    }

    public InterestList update(InterestListUpdate update, University updateDestination, InterestList interestList) {
        interestList.setReferenceDate(update.referenceDate());
        interestList.setClosingTime(update.closingTime());
        interestList.setDepartureTime(update.departureTime());
        interestList.setArrivalTime(update.arrivalTime());
        interestList.setReturnDepartureTime(update.returnDepartureTime());
        interestList.setReturnArrivalTime(update.returnArrivalTime());
        interestList.setDestination(updateDestination);
        interestList.setListStatus(update.listStatus());
        return interestList;
    }

    public InterestList update(InterestListPatch update, University updateDestination, InterestList interestList) {
        if (update.referenceDate() != null) {
            interestList.setReferenceDate(update.referenceDate());
        }
        if (update.closingTime() != null) {
            interestList.setClosingTime(update.closingTime());
        }
        if (update.departureTime() != null) {
            interestList.setDepartureTime(update.departureTime());
        }
        if (update.arrivalTime() != null) {
            interestList.setArrivalTime(update.arrivalTime());
        }
        if (update.returnDepartureTime() != null) {
            interestList.setReturnDepartureTime(update.returnDepartureTime());
        }
        if (update.returnArrivalTime() != null) {
            interestList.setReturnArrivalTime(update.returnArrivalTime());
        }
        if (updateDestination != null) {
            interestList.setDestination(updateDestination);
        }
        if (update.listStatus() != null) {
            interestList.setListStatus(update.listStatus());
        }
        return interestList;
    }
}
