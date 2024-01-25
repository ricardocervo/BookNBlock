package com.ricardocervo.booknblock.block;

import com.ricardocervo.booknblock.property.Property;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BlockService {

    @Transactional
    BlockResponseDto createBlock(BlockRequestDto blockRequest);

    public Block updateBlock(Long id, Block block) ;

    List<Block> findByProperty(Property property);
}
