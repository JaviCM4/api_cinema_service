package com.example.cinema.controllers;

import com.example.cinema.dtos.adblock.AdBlockPricingRequest;
import com.example.cinema.dtos.adblock.AdBlockPricingResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.adblock.AdBlockPricingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ad-block-pricing/cinemas")
public class AdBlockPricingController {
    private final AdBlockPricingService adBlockPricingService;

    public AdBlockPricingController(AdBlockPricingService adBlockPricingService) {
        this.adBlockPricingService = adBlockPricingService;
    }

    @GetMapping("/{cinemaId}")
    public ResponseEntity<AdBlockPricingResponse> getAdBlockPricing(@PathVariable("cinemaId") UUID cinemaId) throws ResourceNotFoundException {
        AdBlockPricingResponse response = adBlockPricingService.getAdBlockPricing(cinemaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AdBlockPricingResponse>> getAllAdBlockPricings() {
        List<AdBlockPricingResponse> response = adBlockPricingService.getAllAdBlockPricings();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cinemaId}")
    public ResponseEntity<AdBlockPricingResponse> createAdBlockPricing(@PathVariable("cinemaId") UUID cinemaId, @Valid @RequestBody AdBlockPricingRequest request) throws ResourceNotFoundException, ConflictException {
        AdBlockPricingResponse response = adBlockPricingService.createAdBlockPricing(cinemaId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{cinemaId}")
    public ResponseEntity<AdBlockPricingResponse> updateAdBlockPricing(@PathVariable("cinemaId") UUID cinemaId, @Valid @RequestBody AdBlockPricingRequest request) throws ResourceNotFoundException {
        AdBlockPricingResponse response = adBlockPricingService.updateAdBlockPricing(cinemaId, request);
        return ResponseEntity.ok(response);
        }
}
