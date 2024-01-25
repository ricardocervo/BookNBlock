package com.ricardocervo.booknblock.block;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockRequestDto {
    private UUID propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}