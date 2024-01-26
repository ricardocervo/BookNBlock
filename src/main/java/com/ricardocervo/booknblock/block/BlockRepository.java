package com.ricardocervo.booknblock.block;


import com.ricardocervo.booknblock.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlockRepository extends JpaRepository<Block, UUID> {

    List<Block> findByProperty(Property property);

}
