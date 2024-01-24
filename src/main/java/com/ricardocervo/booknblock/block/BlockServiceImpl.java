package com.ricardocervo.booknblock.block;

import com.ricardocervo.booknblock.property.Property;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService{

    private final BlockRepository blockRepository;

    @Override
    public Block createBlock(Block block) {
        return null;
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
