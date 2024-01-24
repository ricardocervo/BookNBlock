package com.ricardocervo.booknblock.booking;

import com.ricardocervo.booknblock.guest.GuestDto;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingGuestUpdateDto {
    private List<GuestDto> guests;

}

