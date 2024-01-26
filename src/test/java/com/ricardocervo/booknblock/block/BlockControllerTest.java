package com.ricardocervo.booknblock.block;

import com.ricardocervo.booknblock.booking.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDate;
import java.util.*;

import static com.ricardocervo.booknblock.booking.BookingControllerTest.asJsonString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BlockControllerTest {

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

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private BlockService blockService;

    private List<User> users = new ArrayList<>();
    private Property property1;

    private Property property2;

    private Property propertyTestOverLappingDates;

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
        propertyTestOverLappingDates = propertyRepository.save(propertyTestOverLappingDates);

    }

    @Test
    void createBlock_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        LocalDate blockStartDate = LocalDate.now().plusDays(3);
        LocalDate blockEndDate = LocalDate.now().plusDays(5);


        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        String reason = "paint the room";
        BlockRequestDto blockRequestDto = new BlockRequestDto(property1.getId(), blockStartDate, blockEndDate, reason);

        mockMvc.perform(post("/api/v1/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(blockRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value(property1.getId().toString()))
                .andExpect(jsonPath("$.startDate").value(blockStartDate.toString()))
                .andExpect(jsonPath("$.endDate").value(blockEndDate.toString()))
                .andExpect(jsonPath("$.reason").value(reason));
    }

    @Test
    void createBlock_ShouldReturnConflict_WhenOverlappingWithAnotherBlock() throws Exception {
        LocalDate blockStartDate = LocalDate.now().plusDays(3);
        LocalDate blockEndDate = LocalDate.now().plusDays(5);

        Block block = createTestBlock(property1, blockStartDate, blockEndDate);
        BlockRequestDto blockRequestDto = new BlockRequestDto(property1.getId(), blockStartDate, blockEndDate, "reason");

        mockMvc.perform(post("/api/v1/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(blockRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createBlock_ShouldReturnConflict_WhenOverlappingWithAnExistingConfirmedBooking() throws Exception {
        LocalDate blockStartDate = LocalDate.now().plusDays(3);
        LocalDate blockEndDate = LocalDate.now().plusDays(5);

        Booking booking = createTestBooking(property1, blockStartDate, blockEndDate);

        BlockRequestDto blockRequestDto = new BlockRequestDto(property1.getId(), blockStartDate, blockEndDate, "reason");


        mockMvc.perform(post("/api/v1/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(blockRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createBlock_ShouldReturnOk_WhenOverlappingWithAnExistingCanceledBooking() throws Exception {
        LocalDate blockStartDate = LocalDate.now().plusDays(3);
        LocalDate blockEndDate = LocalDate.now().plusDays(5);

        Booking booking = createTestBooking(property1, blockStartDate, blockEndDate);
        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);

        BlockRequestDto blockRequestDto = new BlockRequestDto(property1.getId(), blockStartDate, blockEndDate, "reason");


        mockMvc.perform(post("/api/v1/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(blockRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createBlock_ShouldReturnBadRequest_WhenMissingParameters() throws Exception {
        LocalDate blockStartDate = LocalDate.now().plusDays(3);
        LocalDate blockEndDate = LocalDate.now().plusDays(5);

        Booking booking = createTestBooking(property1, blockStartDate, blockEndDate);
        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);

        BlockRequestDto blockRequestDto = new BlockRequestDto(property1.getId(), null, blockEndDate, "reason");


        mockMvc.perform(post("/api/v1/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(blockRequestDto)))
                .andExpect(status().isBadRequest());
    }



    private Block createTestBlock(Property property) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        return createTestBlock(property, startDate, endDate);
    }

    private Block createTestBlock(Property property, LocalDate startDate, LocalDate endDate) {
        BlockRequestDto blockRequest = new BlockRequestDto();

        blockRequest.setStartDate(startDate);
        blockRequest.setEndDate(endDate);
        blockRequest.setPropertyId(property.getId());
        blockRequest.setReason("Paint the room");

        BlockResponseDto response = blockService.createBlock(blockRequest);
        return blockRepository.findById(response.getId()).get();

    }

    private Booking createTestBooking() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        return createTestBooking(property1, startDate, endDate);
    }

    private Booking createTestBooking(Property property, LocalDate startDate, LocalDate endDate) {
        BookingRequestDto bookingRequest = new BookingRequestDto();
        bookingRequest.setStartDate(startDate);
        bookingRequest.setEndDate(endDate);
        bookingRequest.setGuests(Collections.singletonList(GuestDto.builder().name("Guest 1").email("email@email.com").build()));
        bookingRequest.setPropertyId(property.getId());
        BookingResponseDto response = bookingService.createBooking(bookingRequest);
        return bookingRepository.findById(UUID.fromString(response.getId())).get();

    }
}
