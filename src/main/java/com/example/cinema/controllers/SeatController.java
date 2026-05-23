package com.example.cinema.controllers;

import com.example.cinema.dtos.theater.response.SeatResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.theater.inteface.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class SeatController {

    private final SeatService seatService;

    @Autowired
    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/v1/theaters/{theaterId}/seats")
    public ResponseEntity<List<SeatResponse>> findByTheaterId(@PathVariable UUID theaterId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(seatService.findByTheaterId(theaterId));
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','CINEMA_ADMIN')")
    @PatchMapping("/v1/seats/{seatId}/toggle")
    public ResponseEntity<Void> toggleSeatStatus(@PathVariable UUID seatId)
            throws ResourceNotFoundException {
        seatService.toggleSeatStatus(seatId);
        return ResponseEntity.noContent().build();
    }
}
