package com.ricardocervo.booknblock.property;


import com.ricardocervo.booknblock.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private String name;
    private String location;
    private String description;

}

