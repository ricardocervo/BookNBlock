package com.ricardocervo.booknblock.booking;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody @Valid BookingRequestDto bookingRequest) {
        Booking newBooking = bookingService.createBooking(bookingRequest);
        return ResponseEntity.ok(newBooking);

    }

}

