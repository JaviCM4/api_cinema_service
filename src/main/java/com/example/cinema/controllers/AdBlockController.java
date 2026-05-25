package com.example.cinema.controllers;

import com.example.cinema.dtos.adblock.AdBlockNowResponse;
import com.example.cinema.dtos.adblock.AdBlockRequest;
import com.example.cinema.dtos.adblock.AdBlockResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.adblock.AdBlockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/cinemas/ad-blocks")
public class AdBlockController {
    private final AdBlockService adBlockService;

    public AdBlockController(AdBlockService adBlockService) {
        this.adBlockService = adBlockService;
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','CINEMA_ADMIN')")
    @PostMapping("/{cinemaId}")
    public ResponseEntity<AdBlockResponse> createAdBlock(@PathVariable("cinemaId") UUID cinemaId, @Valid @RequestBody AdBlockRequest request) throws ResourceNotFoundException, ConflictException {
        AdBlockResponse response = adBlockService.createAdBlock(cinemaId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','CINEMA_ADMIN')")
    @GetMapping("/{cinemaId}")
    public ResponseEntity<List<AdBlockResponse>> getAdBlocksByCinemaId(@PathVariable("cinemaId") UUID cinemaId) throws ResourceNotFoundException {
        List<AdBlockResponse> response = adBlockService.getAdBlocksByCinemaId(cinemaId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping
    public ResponseEntity<List<AdBlockResponse>> getAllAdBlocks() {
        List<AdBlockResponse> response = adBlockService.getAllAdBlocks();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{cinemaId}/current")
    public ResponseEntity<AdBlockNowResponse> getCurrentAdBlockStatus(@PathVariable("cinemaId") UUID cinemaId) throws ResourceNotFoundException {
        AdBlockNowResponse response = adBlockService.getCurrentAdBlockStatus(cinemaId);
        return ResponseEntity.ok(response);
    }
}
