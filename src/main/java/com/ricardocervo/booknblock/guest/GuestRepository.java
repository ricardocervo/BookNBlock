package com.ricardocervo.booknblock.guest;


import com.ricardocervo.booknblock.block.Block;
import com.ricardocervo.booknblock.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuestRepository extends JpaRepository<Guest, UUID> {
}
