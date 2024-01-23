package com.ricardocervo.booknblock.user;

public interface UserService {
    public User createUser(User user);

    public User updateUser(Long id, User user);
}
