package com.ricardocervo.booknblock.user;

import com.ricardocervo.booknblock.exceptions.BadRequestException;
import com.ricardocervo.booknblock.exceptions.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("User already exists");
        };

        user.setPassword(passwordEncoder().encode(user.getPassword()));
        return userRepository.save(user);

    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
