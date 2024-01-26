package com.ricardocervo.booknblock.booking;

import com.ricardocervo.booknblock.guest.GuestDto;
import jakarta.persistence.Entity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingGuestUpdateDto {

    @NotNull
    @Valid
    private List<GuestDto> guests;

}

