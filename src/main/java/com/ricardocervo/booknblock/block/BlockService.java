package com.ricardocervo.booknblock.block;

import com.ricardocervo.booknblock.property.Property;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface BlockService {

    @Transactional
    BlockResponseDto createBlock(BlockRequestDto blockRequest);

    Block getBlockOrThrowException(UUID blockId);

    List<Block> findByProperty(Property property);

    BlockResponseDto updateBlock(UUID blockId, BlockUpdateDto blockUpdateDto);
}
