package com.ricardocervo.booknblock.block;

import com.ricardocervo.booknblock.booking.Booking;
import com.ricardocervo.booknblock.booking.BookingRepository;
import com.ricardocervo.booknblock.booking.BookingStatus;
import com.ricardocervo.booknblock.exceptions.ConflictException;
import com.ricardocervo.booknblock.exceptions.ResourceNotFoundException;
import com.ricardocervo.booknblock.property.Property;
import com.ricardocervo.booknblock.property.PropertyService;
import com.ricardocervo.booknblock.security.SecurityService;
import com.ricardocervo.booknblock.utils.DatesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {
    private final BlockRepository blockRepository;
    private final PropertyService propertyService;
    private final BookingRepository bookingRepository;
    private final SecurityService securityService;

    @Override
    @Transactional
    public BlockResponseDto createBlock(BlockRequestDto blockRequest) {
        validateBlockRequest(blockRequest);

        Property property = propertyService.getPropertyOrThrowException(blockRequest.getPropertyId());
        securityService.authorizeBlock(property);
        Block block = new Block();
        block.setProperty(property);
        block.setStartDate(blockRequest.getStartDate());
        block.setEndDate(blockRequest.getEndDate());
        block.setReason(blockRequest.getReason());

        validateBlockDates(block, property);

        block = blockRepository.save(block);

        return convertToBlockResponseDto(block);
    }


    @Override
    public BlockResponseDto updateBlock(UUID blockId, BlockUpdateDto blockUpdateDto) {
        Block block = getBlockOrThrowException(blockId);
        securityService.authorizeBlock(block.getProperty());

        block.setStartDate(blockUpdateDto.getStartDate());
        block.setEndDate(blockUpdateDto.getEndDate());
        block.setReason(blockUpdateDto.getReason());

        validateBlockDates(block, block.getProperty());

        block = blockRepository.save(block);
        return convertToBlockResponseDto(block);
    }

    @Override
    public void deleteBlock(UUID blockId) {
        Block block = getBlockOrThrowException(blockId);
        securityService.authorizeBlock(block.getProperty());
        blockRepository.delete(block);
    }

    private void validateBlockRequest(BlockRequestDto blockRequest) {
        if (blockRequest.getStartDate() == null || blockRequest.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null.");
        }

        if (blockRequest.getStartDate().isAfter(blockRequest.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

    }


    private void validateBlockDates(Block block, Property property) {
        List<Block> existingBlocks = blockRepository.findByProperty(property);
        for (Block existingBlock : existingBlocks) {

            if (!existingBlock.equals(block) &&
                    DatesUtils.isOverlappingDates(
                            block.getStartDate(),
                            block.getEndDate(),
                            existingBlock.getStartDate(),
                            existingBlock.getEndDate())) {
                throw new ConflictException("The block dates are overlapping with an existing block.");

            }
        }

        List<Booking> existingBookings = bookingRepository.findByProperty(property);
        for (Booking existingBooking : existingBookings) {
            if (existingBooking.getStatus() != BookingStatus.CANCELED &&
                    DatesUtils.isOverlappingDates(
                            existingBooking.getStartDate(),
                            existingBooking.getEndDate(),
                            block.getStartDate(),
                            block.getEndDate())) {
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
    public Block getBlockOrThrowException(UUID blockId) {
        return blockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found with id: " + blockId));
    }

    @Override
    public List<Block> findByProperty(Property property) {
        return blockRepository.findByProperty(property);
    }
}
