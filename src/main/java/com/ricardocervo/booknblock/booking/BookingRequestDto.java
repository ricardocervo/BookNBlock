package com.ricardocervo.booknblock.booking;

import com.ricardocervo.booknblock.guest.Guest;
import com.ricardocervo.booknblock.guest.GuestDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDto {

    private UUID propertyId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @Size(min = 1)
    private List<GuestDto> guests;
}
