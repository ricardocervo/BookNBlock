package com.ricardocervo.booknblock.booking;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

public interface BookingService {

    public BookingResponseDto createBooking(BookingRequestDto bookingRequest);

    BookingResponseDto cancelBooking(UUID bookingId);

    BookingResponseDto updateBookingDates(UUID bookingId, BookingDateUpdateDto dateUpdateDto);

    BookingResponseDto updateBookingGuests(UUID bookingId, BookingGuestUpdateDto guestUpdateDto);

    BookingResponseDto rebookCancelledBooking(UUID bookingId);


    void deleteBooking(UUID bookingId);

    BookingResponseDto getBookingById(UUID bookingId);
}

