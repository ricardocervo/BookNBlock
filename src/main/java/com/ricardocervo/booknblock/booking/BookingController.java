package com.ricardocervo.booknblock.booking;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;


    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestBody @Valid BookingRequestDto bookingRequest) {
        BookingResponseDto newBooking = bookingService.createBooking(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);

    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponseDto> cancelBooking(@PathVariable UUID bookingId) {
        BookingResponseDto canceledBooking = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(canceledBooking);
    }

    @PatchMapping("/{bookingId}/dates")
    public ResponseEntity<BookingResponseDto> updateBookingDates(@PathVariable UUID bookingId,
                                                                 @RequestBody @Valid BookingDateUpdateDto dateUpdateDto) {
        BookingResponseDto updatedBooking = bookingService.updateBookingDates(bookingId, dateUpdateDto);
        return ResponseEntity.ok(updatedBooking);
    }

    @PatchMapping("/{bookingId}/guests")
    public ResponseEntity<BookingResponseDto> updateBookingGuests(@PathVariable UUID bookingId,
                                                                  @RequestBody @Valid BookingGuestUpdateDto guestUpdateDto) {
        BookingResponseDto updatedBooking = bookingService.updateBookingGuests(bookingId, guestUpdateDto);
        return ResponseEntity.ok(updatedBooking);
    }

    @PatchMapping("/{bookingId}/rebook")
    public ResponseEntity<BookingResponseDto> rebookCancelledBooking(@PathVariable UUID bookingId) {
        BookingResponseDto rebookedBooking = bookingService.rebookCancelledBooking(bookingId);
        return ResponseEntity.ok(rebookedBooking);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> deleteBooking(@PathVariable UUID bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable UUID bookingId) {
        BookingResponseDto bookingDto = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(bookingDto);
    }


}

