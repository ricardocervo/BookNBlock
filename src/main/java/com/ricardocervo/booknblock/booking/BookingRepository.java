package com.ricardocervo.booknblock.booking;


import com.ricardocervo.booknblock.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByPropertyAndStatusNot(Property property, BookingStatus status);

}

