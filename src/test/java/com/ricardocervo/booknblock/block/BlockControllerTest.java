package com.ricardocervo.booknblock.block;

import com.ricardocervo.booknblock.BaseTest;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BlockControllerTest extends BaseTest {

    @Test
    void createBlock_ShouldReturnOk_WhenUserIsPropertyOwnerAndRequestIsValid() throws Exception {
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
    void createBlock_ShouldReturnOk_WhenUserIsPropertyManagerAndRequestIsValid() throws Exception {
        LocalDate blockStartDate = LocalDate.now().plusDays(3);
        LocalDate blockEndDate = LocalDate.now().plusDays(5);


        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        String reason = "paint the room";
        BlockRequestDto blockRequestDto = new BlockRequestDto(property1.getId(), blockStartDate, blockEndDate, reason);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(propertyManager.getEmail(), propertyManager.getPassword(), Collections.emptyList())
        );

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
    void createBlock_ShouldReturnForbidden_WhenUserNotPropertyOwnerOrManager() throws Exception {
        LocalDate blockStartDate = LocalDate.now().plusDays(3);
        LocalDate blockEndDate = LocalDate.now().plusDays(5);


        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        String reason = "paint the room";
        BlockRequestDto blockRequestDto = new BlockRequestDto(property1.getId(), blockStartDate, blockEndDate, reason);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser.getEmail(), otherUser.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(post("/api/v1/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(blockRequestDto)))
                .andExpect(status().isForbidden());
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

    @Test
    void updateBlock_ShouldReturnOk_WhenUserIsPropertyOwnerAndRequestIsValid() throws Exception {
        Block block = createTestBlock(property1);

        LocalDate updatedStartDate = LocalDate.now().plusDays(4);
        LocalDate updatedEndDate = LocalDate.now().plusDays(6);
        String updatedReason = "updated reason";
        BlockUpdateDto blockUpdateDto = new BlockUpdateDto(updatedStartDate, updatedEndDate, updatedReason);
        String blockUpdateJson = asJsonString(blockUpdateDto);

        mockMvc.perform(put("/api/v1/blocks/" + block.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blockUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(block.getId().toString()))
                .andExpect(jsonPath("$.startDate").value(updatedStartDate.toString()))
                .andExpect(jsonPath("$.endDate").value(updatedEndDate.toString()))
                .andExpect(jsonPath("$.reason").value(updatedReason))
                .andDo(print());
    }

    @Test
    void updateBlock_ShouldReturnForbidden_WhenUserIsNotPropertyOwnerOrManager() throws Exception {
        Block block = createTestBlock(property1);

        LocalDate updatedStartDate = LocalDate.now().plusDays(4);
        LocalDate updatedEndDate = LocalDate.now().plusDays(6);
        String updatedReason = "updated reason";
        BlockUpdateDto blockUpdateDto = new BlockUpdateDto(updatedStartDate, updatedEndDate, updatedReason);
        String blockUpdateJson = asJsonString(blockUpdateDto);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser.getEmail(), otherUser.getPassword(), Collections.emptyList())
        );


        mockMvc.perform(put("/api/v1/blocks/" + block.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blockUpdateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateBlock_ShouldReturnOk_WhenUserIsPropertyManagerAndRequestIsValid() throws Exception {
        Block block = createTestBlock(property1);

        LocalDate updatedStartDate = LocalDate.now().plusDays(4);
        LocalDate updatedEndDate = LocalDate.now().plusDays(6);
        String updatedReason = "updated reason";
        BlockUpdateDto blockUpdateDto = new BlockUpdateDto(updatedStartDate, updatedEndDate, updatedReason);
        String blockUpdateJson = asJsonString(blockUpdateDto);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(propertyManager.getEmail(), propertyManager.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(put("/api/v1/blocks/" + block.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blockUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(block.getId().toString()))
                .andExpect(jsonPath("$.startDate").value(updatedStartDate.toString()))
                .andExpect(jsonPath("$.endDate").value(updatedEndDate.toString()))
                .andExpect(jsonPath("$.reason").value(updatedReason))
                .andDo(print());
    }

    @Test
    void updateBlock_ShouldReturnConflict_WhenOverlappingWithAnotherBlock() throws Exception {
        Block block1 = createTestBlock(property1);

        //a block that starts 1 day after the previous block
        Block block2 = createTestBlock(property1, block1.getEndDate().plusDays(1), block1.getEndDate().plusDays(5));

        //now tries to update the first block overlapping dates of the second block
        LocalDate updatedStartDate = block1.getStartDate().minusDays(1);
        LocalDate updatedEndDate = block1.getEndDate().plusDays(9);

        String updatedReason = "updated reason";
        BlockUpdateDto blockUpdateDto = new BlockUpdateDto(updatedStartDate, updatedEndDate, updatedReason);
        String blockUpdateJson = asJsonString(blockUpdateDto);

        mockMvc.perform(put("/api/v1/blocks/" + block1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blockUpdateJson))
                .andExpect(status().isConflict());
    }

    @Test
    void updateBlock_ShouldReturnNotFound_WhenBlockDoesNotExist() throws Exception {
        LocalDate updatedStartDate = LocalDate.now().plusDays(4);
        LocalDate updatedEndDate = LocalDate.now().plusDays(6);

        String updatedReason = "updated reason";
        BlockUpdateDto blockUpdateDto = new BlockUpdateDto(updatedStartDate, updatedEndDate, updatedReason);
        String blockUpdateJson = asJsonString(blockUpdateDto);

        mockMvc.perform(put("/api/v1/blocks/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blockUpdateJson))
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteBlock_ShouldReturnNoContent_WhenBlockExists() throws Exception {
        Block block = createTestBlock(property1);

        mockMvc.perform(delete("/api/v1/blocks/" + block.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBlock_ShouldNotFound_WhenBlockDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/v1/blocks/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBlock_ShouldReturnForbidden_WhenUserNotPropertyOwnerOrManager() throws Exception {
        Block block = createTestBlock(property1);


        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser.getEmail(), otherUser.getPassword(), Collections.emptyList())
        );

        mockMvc.perform(delete("/api/v1/blocks/" + block.getId()))
                .andExpect(status().isForbidden());
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
