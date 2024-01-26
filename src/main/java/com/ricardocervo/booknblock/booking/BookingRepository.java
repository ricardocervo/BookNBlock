package com.ricardocervo.booknblock.booking;


import com.ricardocervo.booknblock.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByPropertyAndStatusNot(Property property, BookingStatus status);

    List<Booking> findByProperty(Property property);
}

