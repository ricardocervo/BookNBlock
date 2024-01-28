package com.ricardocervo.booknblock.booking;

import com.ricardocervo.booknblock.guest.Guest;
import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ToString.Exclude
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<Guest> guests;


}


