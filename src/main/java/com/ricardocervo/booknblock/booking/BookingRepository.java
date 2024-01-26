package com.ricardocervo.booknblock.booking;


import com.ricardocervo.booknblock.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByPropertyAndStatusNot(Property property, BookingStatus status);

    List<Booking> findByProperty(Property property);

    @Query("SELECT b FROM Booking b JOIN FETCH b.guests WHERE b.id = :id")
    Optional<Booking> findByIdWithGuests(@Param("id") UUID id);

}

