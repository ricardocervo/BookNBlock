package com.ricardocervo.booknblock.block;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping
    public ResponseEntity<BlockResponseDto> createBlock(@RequestBody BlockRequestDto blockRequest) {
        BlockResponseDto blockDto = blockService.createBlock(blockRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(blockDto);
    }

    @PutMapping("/{blockId}")
    public ResponseEntity<BlockResponseDto> updateBlock(@PathVariable UUID blockId, @RequestBody BlockUpdateDto blockUpdateDto) {
        BlockResponseDto updatedBlock = blockService.updateBlock(blockId, blockUpdateDto);
        return ResponseEntity.ok(updatedBlock);
    }

    @DeleteMapping("/{blockId}")
    public ResponseEntity<?> deleteBlock(@PathVariable UUID blockId) {
        blockService.deleteBlock(blockId);
        return ResponseEntity.noContent().build();
    }

}
