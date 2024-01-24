package com.ricardocervo.booknblock.booking;

import com.ricardocervo.booknblock.block.Block;
import com.ricardocervo.booknblock.block.BlockRepository;
import com.ricardocervo.booknblock.exceptions.BadRequestException;
import com.ricardocervo.booknblock.exceptions.ConflictException;
import com.ricardocervo.booknblock.property.PropertyRepository;
import com.ricardocervo.booknblock.user.User;
import com.ricardocervo.booknblock.user.UserRepository;
import com.ricardocervo.booknblock.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public Booking createBooking(BookingRequestDto bookingRequest)  {



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User guest = userRepository.findByEmail(userEmail).orElseThrow(() -> new BadRequestException("User not found"));

        Booking newBooking = new Booking();
        newBooking.setGuest(guest);
        newBooking.setProperty(propertyRepository.findById(UUID.fromString(bookingRequest.getPropertyId())).orElseThrow(() -> new BadRequestException("Property not found")));
        newBooking.setStartDate(bookingRequest.getStartDate());
        newBooking.setEndDate(bookingRequest.getEndDate());
        newBooking.setStatus(BookingStatus.CONFIRMED);

        validateAndSaveBooking(newBooking);
        return newBooking;
    }

    private void validateAndSaveBooking(Booking booking)  {
        if (isOverlappingWithExistingBooking(booking)) {
            throw new ConflictException("Booking dates are overlapping with an existing booking.");
        }

        if (isOverlappingWithBlock(booking)) {
            throw new ConflictException("Booking dates are overlapping with a block.");
        }

        bookingRepository.save(booking);
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
