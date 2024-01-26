package com.ricardocervo.booknblock.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ricardocervo.booknblock.guest.GuestDto;
import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.property.PropertyRepository;
import com.ricardocervo.booknblock.role.Role;
import com.ricardocervo.booknblock.role.RoleRepository;
import com.ricardocervo.booknblock.user.User;
import com.ricardocervo.booknblock.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    private List<User> users = new ArrayList<>();
    private Property property1;

    private Property property2;

    private Property propertyTestOverLappingDates;

    @BeforeEach
    public void setUp() {
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

        User userTest1 = new User();
        userTest1.setName("User Test 1");
        userTest1.setPassword("pass1");
        userTest1.setEmail("email1@email.com");
        userTest1.setRoles(new HashSet<>());
        userTest1.getRoles().add(roleAdmin);
        userRepository.save(userTest1);

        users.add(userTest1);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userTest1.getEmail(), userTest1.getPassword(), Collections.emptyList())
        );
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();


        property1 = new Property();
        property1.setName("Property1");
        property1.setOwner(userTest1);
        property1.setDescription("Property1 - description");
        property1.setLocation("Porto Alegre");
        property1 = propertyRepository.save(property1);


        property2 = new Property();
        property2.setOwner(userTest1);
        property2.setName("Property2");
        property2.setDescription("Property2 - description");
        property2.setLocation("New York");
        property2 = propertyRepository.save(property2);

        propertyTestOverLappingDates = new Property();
        propertyTestOverLappingDates.setOwner(userTest1);
        propertyTestOverLappingDates.setName("Property2");
        propertyTestOverLappingDates.setDescription("Property2 - description");
        propertyTestOverLappingDates.setLocation("New York");
        propertyTestOverLappingDates  = propertyRepository.save(propertyTestOverLappingDates);

    }

    @Test
    void createBooking_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                property1.getId(), startDate, endDate,
                Collections.singletonList(GuestDto.builder().name("Guest 1").email("email1@gmail1.com").build()));

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookingRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.startDate").value(startDate.toString()))
                .andExpect(jsonPath("$.endDate").value(endDate.toString()))
                .andExpect(jsonPath("$.status").value(BookingStatus.CONFIRMED.toString()))
                .andExpect(jsonPath("$.owner.name").value(users.get(0).getName()))
                .andExpect(jsonPath("$.owner.email").value(users.get(0).getEmail()))
                .andExpect(jsonPath("$.guests", hasSize(1)))
                .andExpect(jsonPath("$.guests[0].name").value("Guest 1"))
                .andExpect(jsonPath("$.guests[0].email").value("email1@gmail1.com"))
                .andReturn();
    }

    @Test
    void createBooking_ShouldReturnBadRequest_NoGuests() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(property1.getId(), startDate, endDate,  null);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookingRequestDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void createBooking_ShouldReturnBadRequest_TwoGuestsSameEmail() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        String sameEmail = "email1@gmail.com";
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                property1.getId(), startDate, endDate,
                Arrays.asList(GuestDto.builder().name("Guest 1").email(sameEmail).build(), GuestDto.builder().name("Guest 2").email(sameEmail).build()));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookingRequestDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }



    // Utility method to convert object to JSON string
    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

