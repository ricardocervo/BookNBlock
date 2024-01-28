package com.ricardocervo.booknblock.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDateUpdateDto {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

}

