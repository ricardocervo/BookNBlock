package com.ricardocervo.booknblock.utils;

import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.property.PropertyRepository;
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
    private final PropertyRepository propertyRepository;

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
        Role roleUser = roleRepository.findByName("ROLE_USER").get();

        User manager1 = new User();
        manager1.setName("Manager1");
        manager1.setPassword("pass123");
        manager1.setEmail("manager1@gmail.com");
        manager1.setRoles(new HashSet<>());
        manager1.getRoles().add(roleAdmin);
        userService.createUser(manager1);

        User manager2 = new User();
        manager2.setName("Manager2");
        manager2.setPassword("pass123");
        manager2.setEmail("manager2@gmail.com");
        manager2.setRoles(new HashSet<>());
        manager2.getRoles().add(roleAdmin);
        userService.createUser(manager2);

        User ricardo = new User();
        ricardo.setName("Ricardo Cervo");
        ricardo.setPassword("pass123");
        ricardo.setEmail("ricardo.a.cervo@gmail.com");
        ricardo.setRoles(new HashSet<>());
        ricardo.getRoles().add(roleAdmin);
        userService.createUser(ricardo);

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

        Property property1 = new Property();
        property1.setName("A property");
        property1.setOwner(user1);
        property1.setDescription("A property - description");
        property1.setLocation("Porto Alegre");
        propertyRepository.save(property1);

        Property property2 = new Property();
        property2.setOwner(user2);
        property2.setName("Another property");
        property2.setDescription("Another property - description");
        property2.setLocation("New York");
        propertyRepository.save(property2);

        property1.setManagers(new HashSet<>(List.of(manager1)));
        propertyRepository.save(property1);

        property2.setManagers(new HashSet<>(List.of(manager2)));
        propertyRepository.save(property2);
    }
}
