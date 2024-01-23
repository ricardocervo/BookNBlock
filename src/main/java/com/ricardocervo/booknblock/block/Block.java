package com.ricardocervo.booknblock.block;


import com.ricardocervo.booknblock.property.Property;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long propertyId;
    private Long ownerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;

}
