package com.guilhermesemog.unimove.security;

import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public UserSecurity(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    public boolean isOwner(Long id, Authentication authentication) {
        return userRepository.findByCpf(authentication.getName())
                .map(user -> user.getId().equals(id))
                .orElse(false);
    }

    public boolean isBookingOwner(Long bookingId, Authentication authentication) {
        return bookingRepository.findById(bookingId)
                .map(booking -> booking.getStudent().getUser().getCpf().equals(authentication.getName()))
                .orElse(false);
    }
}
