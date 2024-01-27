package com.ricardocervo.booknblock.infra;

import com.ricardocervo.booknblock.booking.Booking;
import com.ricardocervo.booknblock.booking.BookingRepository;
import com.ricardocervo.booknblock.exceptions.ResourceNotFoundException;
import com.ricardocervo.booknblock.exceptions.UnauthorizedException;
import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.user.User;
import com.ricardocervo.booknblock.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class SecurityService {

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    public User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        if (email == null) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    public void authorizeBookingUpdate(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        authorizeBookingUpdate(booking);
    }
    public  void authorizeBookingUpdate(Booking booking) {
        if (!getLoggedUser().equals(booking.getOwner())) {
            log.warn("Unauthorized attempt to access a block. User: " + getLoggedUser().getEmail());
            throwGenericUnauthorizedException();
        }
    }

    public  void authorizeBlock(Property property) {
        if (!getLoggedUser().equals(property.getOwner())) {
            if (!property.getManagers().contains(getLoggedUser())) {
                log.warn("Unauthorized attempt to access a block. User: " + getLoggedUser().getEmail());
                throwGenericUnauthorizedException();
            }
        }
    }

    private static void throwGenericUnauthorizedException() {
        throw new UnauthorizedException("You are not allowed to access this resource");
    }
}
