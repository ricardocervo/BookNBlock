package com.ricardocervo.booknblock.security;

import com.ricardocervo.booknblock.booking.Booking;
import com.ricardocervo.booknblock.booking.BookingRepository;
import com.ricardocervo.booknblock.exceptions.ResourceNotFoundException;
import com.ricardocervo.booknblock.exceptions.UnauthorizedException;
import com.ricardocervo.booknblock.user.User;
import com.ricardocervo.booknblock.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    public User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        if (email == null) {
            return null;
        }

        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Logged user email not found in the database!"));
    }

    public void authorize(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        authorize(booking);
    }
    public  void authorize(Booking booking) {
        if (!getLoggedUser().equals(booking.getOwner())) {
            throw new UnauthorizedException("You are not allowed to access this resource");
        }
    }
}
