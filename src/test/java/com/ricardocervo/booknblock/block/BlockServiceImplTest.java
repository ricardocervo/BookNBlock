package com.ricardocervo.booknblock.block;

import com.ricardocervo.booknblock.booking.Booking;
import com.ricardocervo.booknblock.booking.BookingRepository;
import com.ricardocervo.booknblock.booking.BookingStatus;
import com.ricardocervo.booknblock.exceptions.ConflictException;
import com.ricardocervo.booknblock.exceptions.ResourceNotFoundException;
import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.property.PropertyService;
import com.ricardocervo.booknblock.role.Role;
import com.ricardocervo.booknblock.infra.SecurityService;
import com.ricardocervo.booknblock.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class BlockServiceImplTest {

    @InjectMocks
    private BlockServiceImpl blockService;

    @Mock
    private BlockRepository blockRepository;
    @Mock
    private PropertyService propertyService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SecurityService securityService;

    private BlockRequestDto blockRequestDto;
    private Set<Role> roles = Set.of(new Role(UUID.randomUUID(), "ROLE_USER"));
    private User mockedUser = new User(UUID.randomUUID(), "Test User", "password", "test@example.com", roles, null);

    private Property property = new Property(UUID.randomUUID(), mockedUser, "Test Property", "Test Location", "Test Description", Set.of(mockedUser));

    @BeforeEach
    void setUp() {
        blockRequestDto = new BlockRequestDto(
                property.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                "Maintenance"
        );
    }

    @Test
    void createBlock_ShouldCreateBlock_WhenNoConflicts() {
        when(propertyService.getPropertyOrThrowException(property.getId())).thenReturn(property);
        when(blockRepository.findByProperty(property)).thenReturn(Collections.emptyList());
        when(bookingRepository.findByProperty(property)).thenReturn(Collections.emptyList());
        when(blockRepository.save(any(Block.class))).thenAnswer(i -> i.getArguments()[0]);

        BlockResponseDto result = blockService.createBlock(blockRequestDto);

        assertNotNull(result);
        assertEquals(property.getId(), result.getPropertyId());
        verify(blockRepository).save(any(Block.class));
    }

    @Test
    void createBlock_ShouldThrowConflictException_WhenOverlappingWithExistingBlock() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(5);
        Block existingBlock = new Block(UUID.randomUUID(), property, startDate, endDate, "Maintenance");
        when(propertyService.getPropertyOrThrowException(property.getId())).thenReturn(property);
        when(blockRepository.findByProperty(property)).thenReturn(List.of(existingBlock));

        ConflictException thrown = assertThrows(ConflictException.class, () -> {
            blockService.createBlock(blockRequestDto);
        });

        assertEquals("The block dates are overlapping with an existing block.", thrown.getMessage());
    }

    @Test
    void createBlock_ShouldThrowConflictException_WhenOverlappingWithExistingBooking() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(5);
        Booking existingBooking = new Booking(UUID.randomUUID(), property, mockedUser, startDate, endDate, BookingStatus.CONFIRMED, null);
        when(propertyService.getPropertyOrThrowException(property.getId())).thenReturn(property);
        when(blockRepository.findByProperty(property)).thenReturn(Collections.emptyList());
        when(bookingRepository.findByProperty(property)).thenReturn(List.of(existingBooking));

        ConflictException thrown = assertThrows(ConflictException.class, () -> {
            blockService.createBlock(blockRequestDto);
        });

        assertEquals("The block dates are overlapping with an existing booking.", thrown.getMessage());
    }

    @Test
    void updateBlock_ShouldUpdateBlock_WhenNoConflicts() {
        UUID blockId = UUID.randomUUID();
        Block block = new Block(blockId, property, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Maintenance");
        BlockUpdateDto blockUpdateDto = new BlockUpdateDto(LocalDate.now().plusDays(2), LocalDate.now().plusDays(6), "Maintenance2");
        when(blockRepository.findById(blockId)).thenReturn(java.util.Optional.of(block));
        when(blockRepository.findByProperty(property)).thenReturn(Collections.emptyList());
        when(bookingRepository.findByProperty(property)).thenReturn(Collections.emptyList());
        when(blockRepository.save(any(Block.class))).thenAnswer(i -> i.getArguments()[0]);

        BlockResponseDto result = blockService.updateBlock(blockId, blockUpdateDto);

        assertNotNull(result);
        assertEquals(blockId, result.getId());
        assertEquals(blockUpdateDto.getStartDate(), result.getStartDate());
        assertEquals(blockUpdateDto.getEndDate(), result.getEndDate());
        assertEquals(blockUpdateDto.getReason(), result.getReason());
        verify(blockRepository).save(any(Block.class));
    }

    @Test
    void updateBlock_ShouldThrowConflictException_WhenOverlappingWithExistingBlock() {
        UUID blockId1 = UUID.randomUUID();
        UUID blockId2 = UUID.randomUUID();

        Block block = new Block(blockId1, property, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Maintenance");
        Block existingBlock = new Block(blockId2, property, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Maintenance");
        when(blockRepository.findById(blockId1)).thenReturn(java.util.Optional.of(block));
        when(blockRepository.findByProperty(property)).thenReturn(List.of(block, existingBlock));
        BlockUpdateDto blockUpdateDto = new BlockUpdateDto(LocalDate.now().plusDays(2), LocalDate.now().plusDays(6), "Maintenance2");

        ConflictException thrown = assertThrows(ConflictException.class, () -> {
            blockService.updateBlock(blockId1, blockUpdateDto);
        });

        assertEquals("The block dates are overlapping with an existing block.", thrown.getMessage());
    }

    //
    @Test
    void updateBlock_ShouldThrowConflictException_WhenOverlappingWithExistingBooking() {
        UUID blockId = UUID.randomUUID();
        Block blockToUpdate = new Block(blockId, property, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Original Maintenance");
        Booking overlappingBooking = new Booking(UUID.randomUUID(), property, mockedUser, LocalDate.now().plusDays(4), LocalDate.now().plusDays(7), BookingStatus.CONFIRMED, null);

        when(blockRepository.findById(blockId)).thenReturn(java.util.Optional.of(blockToUpdate));
        when(blockRepository.findByProperty(property)).thenReturn(List.of(blockToUpdate));
        when(bookingRepository.findByProperty(property)).thenReturn(List.of(overlappingBooking));

        BlockUpdateDto blockUpdateDto = new BlockUpdateDto(LocalDate.now().plusDays(2), LocalDate.now().plusDays(6), "Updated Maintenance");

        ConflictException thrown = assertThrows(ConflictException.class, () -> {
            blockService.updateBlock(blockId, blockUpdateDto);
        });

        assertEquals("The block dates are overlapping with an existing booking.", thrown.getMessage());
    }

    @Test
    void deleteBlock_ShouldDeleteBlock_WhenBlockExists() {
        UUID blockId = UUID.randomUUID();
        Block block = new Block(blockId, property, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Original Maintenance");
        when(blockRepository.findById(blockId)).thenReturn(Optional.of(block));

        blockService.deleteBlock(blockId);

        verify(blockRepository, times(1)).delete(block);
    }

    @Test
    void deleteBlock_ShouldThrowResourceNotFoundException_WhenBlockDoesNotExist() {
        UUID blockId = UUID.randomUUID();

        when(blockRepository.findById(blockId)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            blockService.deleteBlock(blockId);
        });

        assertEquals("Block not found with id: " + blockId, thrown.getMessage());
        verify(blockRepository, times(0)).delete(any(Block.class));
    }
}
