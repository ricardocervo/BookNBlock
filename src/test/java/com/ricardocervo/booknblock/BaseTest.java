package com.ricardocervo.booknblock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ricardocervo.booknblock.block.BlockRepository;
import com.ricardocervo.booknblock.block.BlockService;
import com.ricardocervo.booknblock.booking.BookingRepository;
import com.ricardocervo.booknblock.booking.BookingService;
import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.property.PropertyRepository;
import com.ricardocervo.booknblock.role.Role;
import com.ricardocervo.booknblock.role.RoleRepository;
import com.ricardocervo.booknblock.user.User;
import com.ricardocervo.booknblock.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BaseTest {











    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected BookingService bookingService;

    @Autowired
    protected BookingRepository bookingRepository;
    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PropertyRepository propertyRepository;

    @Autowired
    protected BlockRepository blockRepository;

    @Autowired
    protected BlockService blockService;

    protected Property property1;

    private Property property2;

    protected Property propertyTestOverLappingDates;

    protected User propertyOwner;
    protected User propertyManager;
    protected User otherUser;

    @BeforeEach
    public void setUp() {
        blockRepository.deleteAll();
        bookingRepository.deleteAll();
        propertyRepository.deleteAll();
        userRepository.deleteAll();


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

        propertyOwner = new User();
        propertyOwner.setName("Property Owner");
        propertyOwner.setPassword("pass1");
        propertyOwner.setEmail("owner@email.com");
        propertyOwner.setRoles(new HashSet<>());
        propertyOwner.getRoles().add(roleAdmin);
        userRepository.save(propertyOwner);

        propertyManager = new User();
        propertyManager.setName("Property Manager");
        propertyManager.setPassword("pass1");
        propertyManager.setEmail("manager@email.com");
        propertyManager.setRoles(new HashSet<>());
        propertyManager.getRoles().add(roleAdmin);
        userRepository.save(propertyManager);

        otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setPassword("pass1");
        otherUser.setEmail("other_user@email.com");
        otherUser.setRoles(new HashSet<>());
        otherUser.getRoles().add(roleAdmin);
        userRepository.save(otherUser);


        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(propertyOwner.getEmail(), propertyOwner.getPassword(), Collections.emptyList())
        );
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();


        property1 = new Property();
        property1.setName("Property1");
        property1.setOwner(propertyOwner);
        property1.setDescription("Property1 - description");
        property1.setLocation("Porto Alegre");
        property1 = propertyRepository.save(property1);


        property2 = new Property();
        property2.setOwner(propertyOwner);
        property2.setName("Property2");
        property2.setDescription("Property2 - description");
        property2.setLocation("New York");
        property2 = propertyRepository.save(property2);

        propertyTestOverLappingDates = new Property();
        propertyTestOverLappingDates.setOwner(propertyOwner);
        propertyTestOverLappingDates.setName("Property2");
        propertyTestOverLappingDates.setDescription("Property2 - description");
        propertyTestOverLappingDates.setLocation("New York");
        propertyTestOverLappingDates = propertyRepository.save(propertyTestOverLappingDates);

        property1.setManagers(new HashSet<>(List.of(propertyManager)));
        propertyRepository.save(property1);

        property2.setManagers(new HashSet<>(List.of(propertyManager)));
        propertyRepository.save(property2);

    }

    protected String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
