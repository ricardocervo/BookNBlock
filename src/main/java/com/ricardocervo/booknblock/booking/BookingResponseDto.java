package com.ricardocervo.booknblock.booking;

import com.ricardocervo.booknblock.guest.Guest;
import com.ricardocervo.booknblock.guest.GuestDto;
import com.ricardocervo.booknblock.property.PropertyDto;
import com.ricardocervo.booknblock.user.UserDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDto {

    @UUID
    private String id;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private BookingStatus status;

    private UserDto owner;

    private PropertyDto property;

    private List<GuestDto> guests;
}
