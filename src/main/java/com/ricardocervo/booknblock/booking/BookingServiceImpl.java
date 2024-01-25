package com.ricardocervo.booknblock.booking;

import com.ricardocervo.booknblock.block.Block;
import com.ricardocervo.booknblock.block.BlockRepository;
import com.ricardocervo.booknblock.block.BlockService;
import com.ricardocervo.booknblock.exceptions.BadRequestException;
import com.ricardocervo.booknblock.exceptions.ConflictException;
import com.ricardocervo.booknblock.exceptions.ResourceNotFoundException;
import com.ricardocervo.booknblock.guest.Guest;
import com.ricardocervo.booknblock.guest.GuestDto;
import com.ricardocervo.booknblock.guest.GuestRepository;
import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.property.PropertyService;
import com.ricardocervo.booknblock.security.SecurityService;
import com.ricardocervo.booknblock.user.User;
import com.ricardocervo.booknblock.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BlockService blockService;
    private final PropertyService propertyService;
    private final ModelMapper modelMapper;
    private final SecurityService securityService;
    private final GuestRepository guestRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingRequest) {
        validateBookingRequest(bookingRequest);

        User owner = securityService.getLoggedUser();
        Property property = propertyService.getPropertyOrThrowException(bookingRequest.getPropertyId());

        Booking newBooking = prepareNewBooking(bookingRequest, owner, property);
        newBooking = validateAndSaveBooking(newBooking);

        addGuestsToBooking(bookingRequest, owner, newBooking);
        newBooking = bookingRepository.save(newBooking);

        return buildResponseDto(newBooking);
    }

    private Booking prepareNewBooking(BookingRequestDto bookingRequest, User owner, Property property) {
        Booking booking = new Booking();
        booking.setOwner(owner);
        booking.setProperty(property);
        booking.setStartDate(bookingRequest.getStartDate());
        booking.setEndDate(bookingRequest.getEndDate());
        booking.setStatus(BookingStatus.CONFIRMED);
        return booking;
    }

    private void addGuestsToBooking(BookingRequestDto bookingRequest, User owner, Booking booking) {
        List<Guest> guests = buildGuestList(bookingRequest, owner, booking);
        booking.setGuests(guests);
    }


    private void validateBookingRequest(BookingRequestDto bookingRequest) {
        if (!bookingRequest.getIncludeLoggedUserAsGuest() && (bookingRequest.getGuests() == null || bookingRequest.getGuests().isEmpty())) {
            throw new BadRequestException("If logged user is not a guest, you must provide at least one guest.");
        }

        if (bookingRequest.getStartDate().isAfter(bookingRequest.getEndDate())) {
            throw new BadRequestException("End date must be greater or equal to start date");
        }

    }

    private BookingResponseDto buildResponseDto(Booking newBooking) {
        BookingResponseDto response = new BookingResponseDto();
        response.setId(newBooking.getId().toString());
        response.setGuests(newBooking.getGuests().stream().map(guest -> modelMapper.map(guest, GuestDto.class)).collect(Collectors.toList()));
        response.setPropertyId(newBooking.getProperty().getId().toString());
        response.setEndDate(newBooking.getEndDate());
        response.setStartDate(newBooking.getStartDate());
        response.setStatus(newBooking.getStatus());
        response.setOwner(UserDto.builder().email(newBooking.getOwner().getEmail()).name(newBooking.getOwner().getName()).build());
        return response;
    }


    private List<Guest> buildGuestList(BookingRequestDto bookingRequest, User owner, Booking booking) {
        List<Guest> guests = new ArrayList<>();
        if (bookingRequest.getIncludeLoggedUserAsGuest()) {
            guests.add(new Guest(UUID.randomUUID(), owner.getName(), owner.getEmail(), booking));
        }

        if (bookingRequest.getGuests() != null) {
            for (GuestDto guestDto : bookingRequest.getGuests()) {
                Guest guest = modelMapper.map(guestDto, Guest.class);
                guest.setBooking(booking);
                guests.add(guest);
            }
        }

        return guests;
    }

    private Booking validateAndSaveBooking(Booking booking) {
        if (isOverlappingWithExistingBooking(booking)) {
            throw new ConflictException("Booking dates are overlapping with an existing booking.");
        }

        if (isOverlappingWithBlock(booking)) {
            throw new ConflictException("Booking dates are overlapping with a block.");
        }

        return bookingRepository.save(booking);
    }

    private boolean isOverlappingWithExistingBooking(Booking booking) {
        List<Booking> existingBookings = bookingRepository.findByPropertyAndStatusNot(
                booking.getProperty(), BookingStatus.CANCELED);

        return existingBookings.stream().anyMatch(existingBooking ->
                !existingBooking.equals(booking) &&
                        !booking.getStartDate().isAfter(existingBooking.getEndDate()) &&
                        !existingBooking.getStartDate().isBefore(booking.getEndDate())
        );

    }

    private boolean isOverlappingWithBlock(Booking booking) {
        List<Block> blocks = blockService.findByProperty(booking.getProperty());

        return blocks.stream().anyMatch(block ->
                booking.getStartDate().isBefore(block.getEndDate()) &&
                        block.getStartDate().isBefore(booking.getEndDate())
        );
    }

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(UUID bookingId) {
        securityService.authorize(bookingId);

        Booking booking = getBookingOrThrowException(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new ConflictException("The booking is already canceled.");
        }

        booking.setStatus(BookingStatus.CANCELED);
        Booking updatedBooking = bookingRepository.save(booking);

        return buildResponseDto(updatedBooking);
    }


    @Override
    @Transactional
    public BookingResponseDto updateBookingDates(UUID bookingId, BookingDateUpdateDto dateUpdateDto) {
        securityService.authorize(bookingId);

        Booking booking = getBookingOrThrowException(bookingId);

        validateBookingDates(dateUpdateDto.getStartDate(), dateUpdateDto.getEndDate());

        booking.setStartDate(dateUpdateDto.getStartDate());
        booking.setEndDate(dateUpdateDto.getEndDate());

        validateAndSaveBooking(booking);

        return buildResponseDto(booking);
    }

    private void validateBookingDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("End date must be greater than or equal to start date.");
        }
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingGuests(UUID bookingId, BookingGuestUpdateDto guestUpdateDto) {
        securityService.authorize(bookingId);

        Booking booking = getBookingOrThrowException(bookingId);

        guestRepository.deleteAll(booking.getGuests());
        booking.getGuests().clear();

        for (GuestDto guestDto : guestUpdateDto.getGuests()) {
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setBooking(booking);
            booking.getGuests().add(guest);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return buildResponseDto(updatedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto rebookCancelledBooking(UUID bookingId) {
        securityService.authorize(bookingId);
        Booking booking = getBookingOrThrowException(bookingId);

        if (booking.getStatus() != BookingStatus.CANCELED) {
            throw new BadRequestException("Only cancelled bookings can be rebooked.");
        }

        if (isOverlappingWithExistingBooking(booking) || isOverlappingWithBlock(booking)) {
            throw new ConflictException("Rebooking dates are overlapping with an existing booking or block.");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking updatedBooking = bookingRepository.save(booking);

        return buildResponseDto(updatedBooking);
    }

    public void deleteBooking(UUID bookingId) {
        securityService.authorize(bookingId);
        bookingRepository.delete(getBookingOrThrowException(bookingId));
    }


    private Booking getBookingOrThrowException(UUID bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));
    }


    @Override
    public BookingResponseDto getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        return buildResponseDto(booking);
    }


}