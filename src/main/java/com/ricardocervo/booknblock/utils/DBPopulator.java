package com.ricardocervo.booknblock.utils;

import com.ricardocervo.booknblock.role.Role;
import com.ricardocervo.booknblock.role.RoleRepository;
import com.ricardocervo.booknblock.user.User;
import com.ricardocervo.booknblock.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DBPopulator implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;


    @Override
    public void run(String... args) throws Exception {
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_MANAGER");

        roles.forEach(roleName -> {
            Optional<Role> existingRole = roleRepository.findByName(roleName);
            if (existingRole.isEmpty()) {
                Role newRole = new Role();
                newRole.setName(roleName);
                roleRepository.save(newRole);
            }
        });

        Role role = roleRepository.findByName("ROLE_MANAGER").get();

        User user = new User();
        user.setName("Ricardo Antonio Cervo");
        user.setPassword("1234");
        user.setEmail("ricardo.a.cervo@gmail.com");
        user.setRoles(new HashSet<>());
        user.getRoles().add(role);

        userRepository.save(user);



    }
}
