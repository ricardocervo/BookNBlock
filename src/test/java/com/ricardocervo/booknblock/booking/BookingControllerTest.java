package com.ricardocervo.booknblock.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ricardocervo.booknblock.block.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private BlockService blockService;

    private List<User> users = new ArrayList<>();
    private Property property1;

    private Property property2;

    private Property propertyTestOverLappingDates;

    private User userTest1;
    private User userTest2;

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

        userTest1 = new User();
        userTest1.setName("User Test 1");
        userTest1.setPassword("pass1");
        userTest1.setEmail("email1@email.com");
        userTest1.setRoles(new HashSet<>());
        userTest1.getRoles().add(roleAdmin);
        userRepository.save(userTest1);

        users.add(userTest1);


        userTest2 = new User();
        userTest2.setName("User Test 2");
        userTest2.setPassword("pass2");
        userTest2.setEmail("email2@email.com");
        userTest2.setRoles(new HashSet<>());
        userTest2.getRoles().add(roleAdmin);
        userRepository.save(userTest2);

        users.add(userTest2);

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
    void createBooking_ShouldReturnConflict_WhenBlockExists() throws Exception {
        LocalDate blockStartDate = LocalDate.now().plusDays(3);
        LocalDate blockEndDate = LocalDate.now().plusDays(5);
        Block block = createTestBlock(property1, blockStartDate, blockEndDate);


        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                property1.getId(), startDate, endDate,
                Collections.singletonList(GuestDto.builder().name("Guest 1").email("email1@gmail1.com").build()));

         mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookingRequestDto)))
                .andExpect(status().isConflict());
    }




    @Test
    void createBooking_ShouldReturnConflict_WhenOverlappingDates() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                property1.getId(), startDate, endDate,
                Collections.singletonList(GuestDto.builder().name("Guest 1").email("email1@gmail1.com").build()));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookingRequestDto)))
                .andExpect(status().isConflict());
    }


    @Test
    void createBooking_ShouldReturnBadRequest_NoGuests() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(property1.getId(), startDate, endDate, null);

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

    @Test
    void cancelBooking_ShouldReturnOk_WhenBookingExists() throws Exception {
        Booking booking = createTestBooking();

        mockMvc.perform(patch("/api/v1/bookings/" + booking.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId().toString()))
                .andExpect(jsonPath("$.startDate").value(booking.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(booking.getEndDate().toString()))
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.owner.name").value("User Test 1"))
                .andExpect(jsonPath("$.owner.email").value("email1@email.com"))
                .andExpect(jsonPath("$.property.id").value(booking.getProperty().getId().toString()))
                .andExpect(jsonPath("$.property.name").value("Property1"))
                .andExpect(jsonPath("$.property.location").value("Porto Alegre"))
                .andExpect(jsonPath("$.property.description").value("Property1 - description"))
                .andExpect(jsonPath("$.guests", hasSize(1)))
                .andExpect(jsonPath("$.guests[0].name").value("Guest 1"))
                .andExpect(jsonPath("$.guests[0].email").value("email@email.com"))
                .andReturn();

    }

    @Test
    void cancelBooking_ShouldReturnUnauthorized_WhenUserIsNotBookingOwner() throws Exception {
        Booking booking = createTestBooking();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userTest2.getEmail(), userTest2.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(patch("/api/v1/bookings/" + booking.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void cancelBooking_ShouldReturnNotFound_WhenBookingNotExists() throws Exception {

        mockMvc.perform(patch("/api/v1/bookings/" + UUID.randomUUID() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

    }

    @Test
    void cancelBooking_ShouldReturnBadRequest_InvalidUUID() throws Exception {

        mockMvc.perform(patch("/api/v1/bookings/" + "invalid_uuid" + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

    }


    @Test
    void updateBookingDates_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        Booking booking = createTestBooking();
        LocalDate newStartDate = booking.getStartDate().plusDays(2);
        LocalDate newEndDate = booking.getEndDate().plusDays(3);
        BookingDateUpdateDto dateUpdateDto = new BookingDateUpdateDto(newStartDate, newEndDate);

        mockMvc.perform(patch("/api/v1/bookings/" + booking.getId() + "/dates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dateUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId().toString()))
                .andExpect(jsonPath("$.startDate").value(newStartDate.toString()))
                .andExpect(jsonPath("$.endDate").value(newEndDate.toString()))
                .andReturn();

    }

    @Test
    void updateBookingDates_ShouldReturnUnauthorized_WhenUserIsNotBookingOwner() throws Exception {
        Booking booking = createTestBooking();
        LocalDate newStartDate = booking.getStartDate().plusDays(2);
        LocalDate newEndDate = booking.getEndDate().plusDays(3);
        BookingDateUpdateDto dateUpdateDto = new BookingDateUpdateDto(newStartDate, newEndDate);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userTest2.getEmail(), userTest2.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(patch("/api/v1/bookings/" + booking.getId() + "/dates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dateUpdateDto)))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void updateBookingDates_ShouldReturnConflict_WhenOverlappingDateWithAnotherBooking() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        Booking booking2 = createTestBooking(LocalDate.now(), LocalDate.now().plusDays(1));

        LocalDate newStartDate = booking1.getStartDate();
        LocalDate newEndDate = booking1.getEndDate().plusDays(1);

        BookingDateUpdateDto dateUpdateDto = new BookingDateUpdateDto(newStartDate, newEndDate);

        mockMvc.perform(patch("/api/v1/bookings/" + booking2.getId() + "/dates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dateUpdateDto)))
                .andExpect(status().isConflict())
                .andReturn();

    }


    @Test
    void updateBookingGuests_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));

        BookingGuestUpdateDto guestUpdateDto = BookingGuestUpdateDto.builder()
                .guests(List.of(GuestDto.builder().email("guest1@gmail.com").name("name1").build()))
                .build();

        String guestUpdateJson = asJsonString(guestUpdateDto);

        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guestUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking1.getId().toString()))
                .andExpect(jsonPath("$.guests", hasSize(1)))
                .andExpect(jsonPath("$.guests[0].name").value("name1"))
                .andExpect(jsonPath("$.guests[0].email").value("guest1@gmail.com"))
                .andDo(print());
    }

    @Test
    void updateBookingGuests_ShouldReturnUnauthorized_WhenUserIsNotBookingOwner() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        bookingRepository.save(booking1);

        BookingGuestUpdateDto guestUpdateDto = BookingGuestUpdateDto.builder()
                .guests(List.of(GuestDto.builder().email("guest1@gmail.com").name("name1").build()))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userTest2.getEmail(), userTest2.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(guestUpdateDto)))
                .andExpect(status().isUnauthorized());

    }


    @Test
    void updateBookingGuests_ShouldReturnBadRequest_WhenGuestEmailIsNull() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));

        BookingGuestUpdateDto guestUpdateDto = BookingGuestUpdateDto.builder()
                .guests(List.of(GuestDto.builder().email(null).name("name1").build()))
                .build();

        String guestUpdateJson = asJsonString(guestUpdateDto);

        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guestUpdateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rebook_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        booking1.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking1);

        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/rebook")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking1.getId().toString()))
                .andExpect(jsonPath("$.startDate").value(booking1.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(booking1.getEndDate().toString()))
                .andExpect(jsonPath("$.status").value(BookingStatus.CONFIRMED.toString()))
                .andExpect(jsonPath("$.owner.name").value(booking1.getOwner().getName()))
                .andExpect(jsonPath("$.owner.email").value(booking1.getOwner().getEmail()))
                .andExpect(jsonPath("$.property.id").value(booking1.getProperty().getId().toString()))
                .andExpect(jsonPath("$.property.name").value(booking1.getProperty().getName()))
                .andExpect(jsonPath("$.property.location").value(booking1.getProperty().getLocation()))
                .andExpect(jsonPath("$.property.description").value(booking1.getProperty().getDescription()));


    }

    @Test
    void rebook_ShouldReturnUnauthorized_WhenUserIsNotBookingOwner() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        booking1.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking1);


        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userTest2.getEmail(), userTest2.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/rebook")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());


    }

    @Test
    void rebook_ShouldReturnConflict_WhenOverlappingDates() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        booking1.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking1);

        Booking booking2 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        bookingRepository.save(booking2);


        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/rebook")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn();

    }

    @Test
    void rebook_ShouldReturnConflict_OverlappedDates() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        bookingRepository.save(booking1);

        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/rebook")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        bookingRepository.save(booking1);

        mockMvc.perform(delete("/api/v1/bookings/" + booking1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }


    @Test
    void delete_ShouldReturnUnauthorized_WhenUserIsNotBookingOwner() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        bookingRepository.save(booking1);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userTest2.getEmail(), userTest2.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(delete("/api/v1/bookings/" + booking1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void delete_ShouldReturnNotFound_WhenBookingNotExists() throws Exception {

        mockMvc.perform(delete("/api/v1/bookings/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    void getBooking_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        booking1 = bookingRepository.findByIdWithGuests(booking1.getId()).get();

        mockMvc.perform(get("/api/v1/bookings/" + booking1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking1.getId().toString()))
                .andExpect(jsonPath("$.startDate").value(booking1.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(booking1.getEndDate().toString()))
                .andExpect(jsonPath("$.status").value(BookingStatus.CONFIRMED.toString()))
                .andExpect(jsonPath("$.owner.name").value(booking1.getOwner().getName()))
                .andExpect(jsonPath("$.owner.email").value(booking1.getOwner().getEmail()))
                .andExpect(jsonPath("$.property.id").value(booking1.getProperty().getId().toString()))
                .andExpect(jsonPath("$.property.name").value(booking1.getProperty().getName()))
                .andExpect(jsonPath("$.property.location").value(booking1.getProperty().getLocation()))
                .andExpect(jsonPath("$.guests", hasSize(booking1.getGuests().size())))
                .andExpect(jsonPath("$.property.description").value(booking1.getProperty().getDescription()));

    }

    @Test
    void getBooking_ShouldReturnNotFound_WhenBookingNotExists() throws Exception {

        mockMvc.perform(get("/api/v1/bookings/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    private Booking createTestBooking() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        return createTestBooking(startDate, endDate);
    }

    private Booking createTestBooking(LocalDate startDate, LocalDate endDate) {
        BookingRequestDto bookingRequest = new BookingRequestDto();
        bookingRequest.setStartDate(startDate);
        bookingRequest.setEndDate(endDate);
        bookingRequest.setGuests(Collections.singletonList(GuestDto.builder().name("Guest 1").email("email@email.com").build()));
        bookingRequest.setPropertyId(property1.getId());
        BookingResponseDto response = bookingService.createBooking(bookingRequest);
        return bookingRepository.findById(UUID.fromString(response.getId())).get();

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

