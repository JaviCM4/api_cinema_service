package com.example.cinema.controllers;

import com.example.cinema.dtos.theater.request.CreateTheaterRequest;
import com.example.cinema.dtos.theater.request.UpdateTheaterRequest;
import com.example.cinema.dtos.theater.response.TheaterClientResponse;
import com.example.cinema.dtos.theater.response.TheaterResponse;
import com.example.cinema.dtos.theater.response.TypeTheaterResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.theater.inteface.TheaterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/theaters")
public class TheaterController {

    private final TheaterService theaterService;

    @Autowired
    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','CINEMA_ADMIN')")
    @PostMapping
    public ResponseEntity<Void> createTheater(@Valid @RequestBody CreateTheaterRequest request)
            throws ResourceNotFoundException, ConflictException {
        theaterService.createTheater(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','CINEMA_ADMIN')")
    @PatchMapping("/{theaterId}")
    public ResponseEntity<Void> updateTheater(@PathVariable UUID theaterId, @Valid @RequestBody UpdateTheaterRequest request)
            throws ResourceNotFoundException {
        theaterService.updateTheater(theaterId, request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','CINEMA_ADMIN')")
    @GetMapping("/types")
    public ResponseEntity<List<TypeTheaterResponse>> getTypeTheaters() {
        return ResponseEntity.ok(theaterService.findAllTypeTheaters());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<TheaterResponse>> findTheatersByCinema(@RequestParam UUID cinemaId) {
        return ResponseEntity.ok(theaterService.findTheatersByCinema(cinemaId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/movie")
    public ResponseEntity<List<TheaterClientResponse>> findTheatersByMovie(@RequestParam UUID movieId) {
        return ResponseEntity.ok(theaterService.findTheatersByMovie(movieId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cinema/{cinemaId}")
    public ResponseEntity<List<TheaterClientResponse>> findTheatersWithShowtimesByCinema(@PathVariable UUID cinemaId) {
        return ResponseEntity.ok(theaterService.findTheatersWithShowtimesByCinema(cinemaId));
    }
}
