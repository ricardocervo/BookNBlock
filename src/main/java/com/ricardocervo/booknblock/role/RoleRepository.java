package com.ricardocervo.booknblock.role;


import com.ricardocervo.booknblock.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
