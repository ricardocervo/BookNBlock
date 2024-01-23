package com.ricardocervo.booknblock.utils;

import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.property.PropertyService;
import com.ricardocervo.booknblock.role.Role;
import com.ricardocervo.booknblock.role.RoleRepository;
import com.ricardocervo.booknblock.user.User;
import com.ricardocervo.booknblock.user.UserService;
import lombok.RequiredArgsConstructor;
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
    private final UserService userService;
    private final PropertyService propertyService;


    @Override
    public void run(String... args) throws Exception {
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

        roles.forEach(roleName -> {
            Optional<Role> existingRole = roleRepository.findByName(roleName);
            if (existingRole.isEmpty()) {
                Role newRole = new Role();
                newRole.setName(roleName);
                roleRepository.save(newRole);
            }
        });

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").get();

        User manager1 = new User();
        manager1.setName("Ricardo Cervo");
        manager1.setPassword("pass123");
        manager1.setEmail("ricardo.a.cervo@gmail.com");
        manager1.setRoles(new HashSet<>());
        manager1.getRoles().add(roleAdmin);
        userService.createUser(manager1);



        Role roleUser = roleRepository.findByName("ROLE_USER").get();

        User user1 = new User();
        user1.setName("Alexa Richmond");
        user1.setPassword("user123");
        user1.setEmail("alexa.richmond@example.com");
        user1.setRoles(new HashSet<>());
        user1.getRoles().add(roleUser);
        userService.createUser(user1);

        User user2 = new User();
        user2.setName("Marcus Wellford");
        user2.setPassword("user123");
        user2.setEmail("marcus.wellford@example.com");
        user2.setRoles(new HashSet<>());
        user2.getRoles().add(roleUser);
        userService.createUser(user2);

        User user3 = new User();
        user3.setName("Sophia Castellano");
        user3.setPassword("user123");
        user3.setEmail("sophia.castellano@example.com");
        user3.setRoles(new HashSet<>());
        user3.getRoles().add(roleUser);
        userService.createUser(user3);

        Property property = new Property();
        property.setName("A property");
        property.setOwner(user1);
        property.setDescription("A property - description");
        property.setLocation("Porto Alegre");
        propertyService.createProperty(property);

        property = new Property();
        property.setOwner(user2);
        property.setName("Another property");
        property.setDescription("Another property - description");
        property.setLocation("New York");
        propertyService.createProperty(property);

    }
}
