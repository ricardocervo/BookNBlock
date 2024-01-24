package com.ricardocervo.booknblock.booking;

import com.ricardocervo.booknblock.block.Block;
import com.ricardocervo.booknblock.block.BlockRepository;
import com.ricardocervo.booknblock.exceptions.BadRequestException;
import com.ricardocervo.booknblock.exceptions.ConflictException;
import com.ricardocervo.booknblock.exceptions.ResourceNotFoundException;
import com.ricardocervo.booknblock.guest.Guest;
import com.ricardocervo.booknblock.guest.GuestDto;
import com.ricardocervo.booknblock.property.PropertyRepository;
import com.ricardocervo.booknblock.user.User;
import com.ricardocervo.booknblock.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ModelMapper modelMapper;

    public BookingResponseDto createBooking(BookingRequestDto bookingRequest)  {

        validateBookingRequest(bookingRequest);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User owner = userRepository.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking newBooking = new Booking();
        newBooking.setOwner(owner);
        newBooking.setProperty(propertyRepository.findById(UUID.fromString(bookingRequest.getPropertyId())).orElseThrow(() -> new ResourceNotFoundException("Property not found")));
        newBooking.setStartDate(bookingRequest.getStartDate());
        newBooking.setEndDate(bookingRequest.getEndDate());
        newBooking.setStatus(BookingStatus.CONFIRMED);

        newBooking = validateAndSaveBooking(newBooking);

        newBooking.setGuests(buildGuestList(bookingRequest, owner, newBooking));
        newBooking = bookingRepository.save(newBooking);


        return buildResponseDto(newBooking);
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
        response.setGuests(newBooking.getGuests().stream().map(guest -> modelMapper.map(guest, GuestDto.class)).collect(Collectors.toList()));
        response.setPropertyId(newBooking.getProperty().getId().toString());
        response.setEndDate(newBooking.getEndDate());
        response.setStartDate(newBooking.getStartDate());
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
                guests.add(guest);
            }
        }

        return guests;
    }

    private Booking validateAndSaveBooking(Booking booking)  {
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
                booking.getStartDate().isBefore(existingBooking.getEndDate()) &&
                        existingBooking.getStartDate().isBefore(booking.getEndDate())
        );
    }

    private boolean isOverlappingWithBlock(Booking booking) {
        List<Block> blocks = blockRepository.findByProperty(booking.getProperty());

        return blocks.stream().anyMatch(block ->
                booking.getStartDate().isBefore(block.getEndDate()) &&
                        block.getStartDate().isBefore(booking.getEndDate())
        );
    }

}
