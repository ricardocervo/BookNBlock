package com.ricardocervo.booknblock.block;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
