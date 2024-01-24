package com.ricardocervo.booknblock.booking;


import com.ricardocervo.booknblock.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findById(UUID id);
    List<Booking> findByPropertyAndStatusNot(Property property, BookingStatus status);

}

