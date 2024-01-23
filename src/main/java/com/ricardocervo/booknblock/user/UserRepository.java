package com.ricardocervo.booknblock.user;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    public Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.roles where u.email = :email")
    public Optional<User> findByEmailFetchRoles(String email);

}

