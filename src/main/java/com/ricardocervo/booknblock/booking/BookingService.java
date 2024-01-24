package com.ricardocervo.booknblock.booking;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public interface BookingService {

    public Booking createBooking(BookingRequestDto bookingRequest);


}

