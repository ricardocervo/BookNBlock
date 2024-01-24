package com.ricardocervo.booknblock.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDto {

    @UUID
    private String propertyId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

}
