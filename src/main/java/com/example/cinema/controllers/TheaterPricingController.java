package com.example.cinema.controllers;

import com.example.cinema.dtos.theater.request.TheaterPrincingRequest;
import com.example.cinema.dtos.theater.response.TheaterPrincingResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.theater.inteface.TheaterPricingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/theaters/{theaterId}/pricing")
public class TheaterPricingController {
    private final TheaterPricingService theaterPricingService;

    public TheaterPricingController(TheaterPricingService theaterPricingService) {
        this.theaterPricingService = theaterPricingService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<TheaterPrincingResponse> getTheaterPricing(@PathVariable UUID theaterId) throws ResourceNotFoundException, ConflictException {
        TheaterPrincingResponse response = theaterPricingService.getTheaterPricing(theaterId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','CINEMA_ADMIN')")
    @PostMapping
    public ResponseEntity<TheaterPrincingResponse> createTheaterPricing(@PathVariable UUID theaterId,@Valid @RequestBody TheaterPrincingRequest dto) throws ResourceNotFoundException, ConflictException {
        TheaterPrincingResponse response = theaterPricingService.createTheaterPricing(theaterId, dto);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','CINEMA_ADMIN')")
    @PutMapping
    public ResponseEntity<TheaterPrincingResponse> updateTheaterPricing(@PathVariable UUID theaterId,@Valid @RequestBody TheaterPrincingRequest dto) throws ResourceNotFoundException {
        TheaterPrincingResponse response = theaterPricingService.updateTheaterPricing(theaterId, dto);
        return ResponseEntity.ok(response);
    }

}
