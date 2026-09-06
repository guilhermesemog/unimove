package com.guilhermesemog.unimove.repository;

import java.util.UUID;
public interface BookingCountView {
    UUID getInterestListId();

    Long getBookingCount();
}
