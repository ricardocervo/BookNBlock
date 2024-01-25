package com.ricardocervo.booknblock.block;

import com.ricardocervo.booknblock.booking.Booking;
import com.ricardocervo.booknblock.booking.BookingRepository;
import com.ricardocervo.booknblock.booking.BookingStatus;
import com.ricardocervo.booknblock.exceptions.ConflictException;
import com.ricardocervo.booknblock.exceptions.ResourceNotFoundException;
import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.property.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService{
    private final BlockRepository blockRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    @Override
    @Transactional
    public BlockResponseDto createBlock(BlockRequestDto blockRequest) {
        validateBlockRequest(blockRequest);

        Property property = propertyRepository.findById(blockRequest.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + blockRequest.getPropertyId()));

        Block block = new Block();
        block.setProperty(property);
        block.setStartDate(blockRequest.getStartDate());
        block.setEndDate(blockRequest.getEndDate());
        block.setReason(blockRequest.getReason());

        validateBlockDates(block, property);

        block = blockRepository.save(block);

        return convertToBlockResponseDto(block);
    }

    private void validateBlockRequest(BlockRequestDto blockRequest) {
        if (blockRequest.getStartDate() == null || blockRequest.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null.");
        }

        if (blockRequest.getStartDate().isAfter(blockRequest.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        // Add more validation as needed (e.g., reason for the block, property ID)
    }


    private void validateBlockDates(Block block, Property property) {
        List<Block> existingBlocks = blockRepository.findByProperty(property);
        for (Block existingBlock : existingBlocks) {
            if (!existingBlock.getId().equals(block.getId()) &&
                    !block.getStartDate().isAfter(existingBlock.getEndDate()) &&
                    !block.getEndDate().isBefore(existingBlock.getStartDate())) {
                throw new ConflictException("The block dates are overlapping with an existing block.");
            }
        }

        List<Booking> existingBookings = bookingRepository.findByProperty(property);
        for (Booking existingBooking : existingBookings) {
            if (existingBooking.getStatus() != BookingStatus.CANCELED &&
                    !block.getStartDate().isAfter(existingBooking.getEndDate()) &&
                    !block.getEndDate().isBefore(existingBooking.getStartDate())) {
                throw new ConflictException("The block dates are overlapping with an existing booking.");
            }
        }
    }




    private BlockResponseDto convertToBlockResponseDto(Block block) {
        return new BlockResponseDto(
                block.getId(),
                block.getProperty().getId(),
                block.getStartDate(),
                block.getEndDate(),
                block.getReason()
        );
    }


    @Override
    public Block updateBlock(Long id, Block block) {
        return null;
    }

    @Override
    public List<Block> findByProperty(Property property) {
        return blockRepository.findByProperty(property);
    }
}
