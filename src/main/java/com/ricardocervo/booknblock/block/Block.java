package com.ricardocervo.booknblock.block;


import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;

}
