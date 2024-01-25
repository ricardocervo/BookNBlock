package com.ricardocervo.booknblock.block;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockUpdateDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}

