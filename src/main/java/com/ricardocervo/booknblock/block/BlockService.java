package com.ricardocervo.booknblock.block;

import com.ricardocervo.booknblock.property.Property;

import java.util.List;

public interface BlockService {

    public Block createBlock(Block block);

    public Block updateBlock(Long id, Block block) ;

    List<Block> findByProperty(Property property);
}
