package com.ricardocervo.booknblock.booking;

import com.ricardocervo.booknblock.block.BlockService;
import com.ricardocervo.booknblock.exceptions.ConflictException;
import com.ricardocervo.booknblock.exceptions.ResourceNotFoundException;
import com.ricardocervo.booknblock.guest.Guest;
import com.ricardocervo.booknblock.guest.GuestDto;
import com.ricardocervo.booknblock.guest.GuestRepository;
import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.property.PropertyService;
import com.ricardocervo.booknblock.role.Role;
import com.ricardocervo.booknblock.infra.SecurityService;
import com.ricardocervo.booknblock.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BlockService blockService;
    @Mock
    private PropertyService propertyService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private SecurityService securityService;
    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Set<Role> roles = Set.of(new Role(UUID.randomUUID(), "ROLE_USER"));
    private User mockedUser = new User(UUID.randomUUID(), "Test User", "password", "test@example.com", roles, null);
    private Property mockedProperty = new Property(UUID.randomUUID(), mockedUser, "Test Property", "Test Location", "Test Description", Set.of(mockedUser));

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBooking_ShouldCreateBooking_WhenRequestIsValid() {
        UUID propertyId = UUID.randomUUID();
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                propertyId,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                Collections.singletonList(new GuestDto(UUID.randomUUID(), "John Doe", "johndoe@example.com"))
        );

        Booking mockedBooking = new Booking(UUID.randomUUID(), mockedProperty, mockedUser, bookingRequestDto.getStartDate(), bookingRequestDto.getEndDate(), BookingStatus.CONFIRMED, null);

        when(securityService.getLoggedUser()).thenReturn(mockedUser);
        when(propertyService.getPropertyOrThrowException(propertyId)).thenReturn(mockedProperty);
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockedBooking);
        when(modelMapper.map(any(GuestDto.class), eq(Guest.class))).thenAnswer(i -> {
            GuestDto guestDto = i.getArgument(0);
            return new Guest(UUID.randomUUID(), guestDto.getName(), guestDto.getEmail(), mockedBooking);
        });

        BookingResponseDto result = bookingService.createBooking(bookingRequestDto);

        assertNotNull(result);
        assertEquals(mockedBooking.getId().toString(), result.getId());
        verify(bookingRepository, times(2)).save(any(Booking.class));
    }

    @Test
    void createBooking_ShouldThrowConflictException_WhenDatesOverlapWithExistingBooking() {
        UUID propertyId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                propertyId,
                startDate,
                endDate,
                Collections.singletonList(new GuestDto(UUID.randomUUID(), "John Doe", "johndoe@example.com"))
        );
        Booking existingBooking = new Booking(UUID.randomUUID(), mockedProperty, mockedUser, startDate, endDate, BookingStatus.CONFIRMED, null);

        when(securityService.getLoggedUser()).thenReturn(mockedUser);
        when(propertyService.getPropertyOrThrowException(propertyId)).thenReturn(mockedProperty);
        when(bookingRepository.findByPropertyAndStatusNot(any(Property.class), any(BookingStatus.class)))
                .thenReturn(List.of(existingBooking));

        ConflictException thrown = assertThrows(ConflictException.class, () -> {
            bookingService.createBooking(bookingRequestDto);
        });

        assertEquals("Booking dates are overlapping with an existing booking.", thrown.getMessage());
    }

    @Test
    void cancelBooking_ShouldCancelBooking_WhenBookingExistsAndIsNotCanceled() {
        UUID bookingId = UUID.randomUUID();

        Booking existingBooking = new Booking(bookingId, mockedProperty, mockedUser, LocalDate.now(), LocalDate.now().plusDays(3), BookingStatus.CONFIRMED, null);
        List<Guest> guests = List.of(new Guest(UUID.randomUUID(), "John Doe", "guest@email.com", existingBooking));
        existingBooking.setGuests(guests);

        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.of(existingBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(existingBooking);

        BookingResponseDto result = bookingService.cancelBooking(bookingId);

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateBookingDates_ShouldUpdateDates_WhenNewDatesAreValid() {
        UUID bookingId = UUID.randomUUID();
        LocalDate newStartDate = LocalDate.now().plusDays(4);
        LocalDate newEndDate = LocalDate.now().plusDays(7);
        BookingDateUpdateDto dateUpdateDto = new BookingDateUpdateDto(newStartDate, newEndDate);

        Booking existingBooking = new Booking(bookingId, mockedProperty, mockedUser, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), BookingStatus.CONFIRMED, null);
        List<Guest> guests = List.of(new Guest(UUID.randomUUID(), "John Doe", "guest@email.com", existingBooking));
        existingBooking.setGuests(guests);
        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.of(existingBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(existingBooking);
        when(blockService.findByProperty(any(Property.class))).thenReturn(Collections.emptyList());
        when(bookingRepository.findByPropertyAndStatusNot(any(Property.class), any(BookingStatus.class)))
                .thenReturn(Collections.emptyList());

        BookingResponseDto updatedBooking = bookingService.updateBookingDates(bookingId, dateUpdateDto);

        assertNotNull(updatedBooking);
        assertEquals(newStartDate, updatedBooking.getStartDate());
        assertEquals(newEndDate, updatedBooking.getEndDate());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateBookingDates_ShouldThrowConflictException_WhenDatesOverlapWithExistingBooking() {
        UUID bookingId = UUID.randomUUID();
        LocalDate newStartDate = LocalDate.now().plusDays(2);
        LocalDate newEndDate = LocalDate.now().plusDays(5);
        BookingDateUpdateDto dateUpdateDto = new BookingDateUpdateDto(newStartDate, newEndDate);
        Booking existingBooking = new Booking(bookingId, mockedProperty, mockedUser, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), BookingStatus.CONFIRMED,null);
        List<Guest> guests = List.of(new Guest(UUID.randomUUID(), "John Doe", "guest@email.com", existingBooking));
        existingBooking.setGuests(guests);
        Booking overlappingBooking = new Booking(UUID.randomUUID(), mockedProperty, mockedUser, LocalDate.now().plusDays(4), LocalDate.now().plusDays(6), BookingStatus.CONFIRMED, null);

        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.of(existingBooking));
        when(blockService.findByProperty(any(Property.class))).thenReturn(Collections.emptyList());
        when(bookingRepository.findByPropertyAndStatusNot(any(Property.class), any(BookingStatus.class)))
                .thenReturn(Collections.singletonList(overlappingBooking));

        ConflictException thrown = assertThrows(ConflictException.class, () -> {
            bookingService.updateBookingDates(bookingId, dateUpdateDto);
        });

        assertEquals("Booking dates are overlapping with an existing booking.", thrown.getMessage());
    }

    @Test
    void rebookCancelledBooking_ShouldRebook_WhenNoDateConflicts() {
        UUID bookingId = UUID.randomUUID();

        Booking cancelledBooking = new Booking(bookingId, mockedProperty, mockedUser, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), BookingStatus.CANCELED, null);
        List<Guest> guests = List.of(new Guest(UUID.randomUUID(), "John Doe", "guest@email.com", cancelledBooking));
        cancelledBooking.setGuests(guests);
        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.of(cancelledBooking));
        when(blockService.findByProperty(any(Property.class))).thenReturn(Collections.emptyList());
        when(bookingRepository.findByPropertyAndStatusNot(any(Property.class), any(BookingStatus.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(cancelledBooking);

        BookingResponseDto rebookedBooking = bookingService.rebookCancelledBooking(bookingId);

        assertNotNull(rebookedBooking);
        assertEquals(BookingStatus.CONFIRMED, rebookedBooking.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void rebookCancelledBooking_ShouldThrowConflictException_WhenDatesOverlapWithExistingBooking() {
        UUID bookingId = UUID.randomUUID();

        Booking cancelledBooking = new Booking(bookingId, mockedProperty, mockedUser, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), BookingStatus.CANCELED, null);
        List<Guest> guests = List.of(new Guest(UUID.randomUUID(), "John Doe", "guest@email.com", cancelledBooking));
        cancelledBooking.setGuests(guests);
        Booking overlappingBooking = new Booking(UUID.randomUUID(), mockedProperty, mockedUser, LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), BookingStatus.CONFIRMED, null);

        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.of(cancelledBooking));
        when(blockService.findByProperty(any(Property.class))).thenReturn(Collections.emptyList());
        when(bookingRepository.findByPropertyAndStatusNot(any(Property.class), any(BookingStatus.class)))
                .thenReturn(Collections.singletonList(overlappingBooking));

        ConflictException thrown = assertThrows(ConflictException.class, () -> {
            bookingService.rebookCancelledBooking(bookingId);
        });

        assertEquals("Rebooking dates are overlapping with an existing booking or block.", thrown.getMessage());
    }

    @Test
    void deleteBooking_ShouldDeleteBooking_WhenBookingExists() {
        UUID bookingId = UUID.randomUUID();
        Booking existingBooking = new Booking(bookingId, mockedProperty, mockedUser, LocalDate.now(), LocalDate.now().plusDays(3), BookingStatus.CONFIRMED, null);

        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.of(existingBooking));
        doNothing().when(bookingRepository).delete(any(Booking.class));

        bookingService.deleteBooking(bookingId);

        verify(bookingRepository).delete(any(Booking.class));
    }

    @Test
    void deleteBooking_ShouldThrowResourceNotFoundException_WhenBookingDoesNotExist() {
        UUID bookingId = UUID.randomUUID();

        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.deleteBooking(bookingId);
        });

        assertEquals("Booking not found with id " + bookingId, thrown.getMessage());
    }

    @Test
    void getBookingById_ShouldReturnBooking_WhenBookingExists() {
        UUID bookingId = UUID.randomUUID();

        Booking existingBooking = new Booking(bookingId, mockedProperty, mockedUser, LocalDate.now(), LocalDate.now().plusDays(3), BookingStatus.CONFIRMED,null);
        List<Guest> guests = List.of(new Guest(UUID.randomUUID(), "John Doe", "guest@email.com", existingBooking));
        existingBooking.setGuests(guests);
        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.of(existingBooking));

        BookingResponseDto result = bookingService.getBookingById(bookingId);

        assertNotNull(result);
        assertEquals(bookingId.toString(), result.getId());
    }

    @Test
    void getBookingById_ShouldThrowResourceNotFoundException_WhenBookingDoesNotExist() {
        UUID bookingId = UUID.randomUUID();

        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.getBookingById(bookingId);
        });

        assertEquals("Booking not found with id: " + bookingId, thrown.getMessage());
    }
}


