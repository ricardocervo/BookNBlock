package com.ricardocervo.booknblock.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDateUpdateDto {
    private LocalDate startDate;
    private LocalDate endDate;

}

