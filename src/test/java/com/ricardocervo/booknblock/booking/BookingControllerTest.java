package com.ricardocervo.booknblock.booking;

import com.ricardocervo.booknblock.BaseTest;
import com.ricardocervo.booknblock.block.Block;
import com.ricardocervo.booknblock.block.BlockRequestDto;
import com.ricardocervo.booknblock.block.BlockResponseDto;
import com.ricardocervo.booknblock.guest.GuestDto;
import com.ricardocervo.booknblock.property.Property;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class BookingControllerTest extends BaseTest {

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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.startDate").value(startDate.toString()))
                .andExpect(jsonPath("$.endDate").value(endDate.toString()))
                .andExpect(jsonPath("$.status").value(BookingStatus.CONFIRMED.toString()))
                .andExpect(jsonPath("$.owner.name").value(propertyOwner.getName()))
                .andExpect(jsonPath("$.owner.email").value(propertyOwner.getEmail()))
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
                .andExpect(jsonPath("$.owner.name").value(propertyOwner.getName()))
                .andExpect(jsonPath("$.owner.email").value(propertyOwner.getEmail()))
                .andExpect(jsonPath("$.property.id").value(booking.getProperty().getId().toString()))
                .andExpect(jsonPath("$.property.name").value(property1.getName()))
                .andExpect(jsonPath("$.property.location").value(property1.getLocation()))
                .andExpect(jsonPath("$.property.description").value(property1.getDescription()))
                .andExpect(jsonPath("$.guests", hasSize(1)))
                .andExpect(jsonPath("$.guests[0].name").value("Guest 1"))
                .andExpect(jsonPath("$.guests[0].email").value("email@email.com"))
                .andReturn();

    }

    @Test
    void cancelBooking_ShouldReturnForbidden_WhenUserIsNotBookingOwner() throws Exception {
        Booking booking = createTestBooking();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser.getEmail(), otherUser.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(patch("/api/v1/bookings/" + booking.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

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
    void updateBookingDates_ShouldReturnForbidden_WhenUserIsNotBookingOwner() throws Exception {
        Booking booking = createTestBooking();
        LocalDate newStartDate = booking.getStartDate().plusDays(2);
        LocalDate newEndDate = booking.getEndDate().plusDays(3);
        BookingDateUpdateDto dateUpdateDto = new BookingDateUpdateDto(newStartDate, newEndDate);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser.getEmail(), otherUser.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(patch("/api/v1/bookings/" + booking.getId() + "/dates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dateUpdateDto)))
                .andExpect(status().isForbidden());

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
    void updateBookingGuests_ShouldReturnForbidden_WhenUserIsNotBookingOwner() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        bookingRepository.save(booking1);

        BookingGuestUpdateDto guestUpdateDto = BookingGuestUpdateDto.builder()
                .guests(List.of(GuestDto.builder().email("guest1@gmail.com").name("name1").build()))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser.getEmail(), otherUser.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(guestUpdateDto)))
                .andExpect(status().isForbidden());

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
    void rebook_ShouldReturnForbidden_WhenUserIsNotBookingOwner() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        booking1.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking1);


        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser.getEmail(), otherUser.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/rebook")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());


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
    void rebook_ShouldReturnConflict_BookingIsNotCanceled() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        bookingRepository.save(booking1);

        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/rebook")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void rebook_ShouldReturnConflict_OverlappedDates() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        booking1.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking1);

        Booking booking2 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        bookingRepository.save(booking1);

        mockMvc.perform(patch("/api/v1/bookings/" + booking1.getId() + "/rebook")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
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
    void delete_ShouldReturnForbidden_WhenUserIsNotBookingOwner() throws Exception {
        Booking booking1 = createTestBooking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        bookingRepository.save(booking1);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser.getEmail(), otherUser.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(delete("/api/v1/bookings/" + booking1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

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



}

