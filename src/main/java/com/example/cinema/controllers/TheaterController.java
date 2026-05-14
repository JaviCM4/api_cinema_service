package com.example.cinema.controllers;

import com.example.cinema.dtos.theater.request.CreateTheaterRequest;
import com.example.cinema.dtos.theater.request.UpdateTheaterRequest;
import com.example.cinema.dtos.theater.response.TheaterResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.theater.inteface.TheaterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/theaters")
public class TheaterController {

    private final TheaterService theaterService;

    @Autowired
    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }

    @PostMapping
    public ResponseEntity<Void> createTheater(
            @Valid @RequestBody CreateTheaterRequest request) throws ResourceNotFoundException, ConflictException {
        theaterService.createTheater(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{theaterId}")
    public ResponseEntity<Void> updateTheater(
            @PathVariable UUID theaterId,
            @Valid @RequestBody UpdateTheaterRequest request) throws ResourceNotFoundException {
        theaterService.updateTheater(theaterId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{theaterId}")
    public ResponseEntity<TheaterResponse> getTheater(
            @PathVariable UUID theaterId) throws ResourceNotFoundException {
        return ResponseEntity.ok(theaterService.getTheater(theaterId));
    }
}
