package com.ricardocervo.booknblock.user;

import java.util.List;
import java.util.Optional;

public interface UserService {
    public User createUser(User user);

    Optional<User> findById(Long id);

    List<User> findAll();
}
