package com.example.cinema.controllers;

import com.example.cinema.dtos.cinema.request.CreateCinemaRequest;
import com.example.cinema.dtos.cinema.request.UpdateCinemaRequest;
import com.example.cinema.dtos.cinema.response.CinemaResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.cinema.inteface.CinemaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/cinemas")
public class CinemaController {

    private final CinemaService cinemaService;

    @Autowired
    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @GetMapping
    public ResponseEntity<List<CinemaResponse>> getAllCinemas() {
        return ResponseEntity.ok(cinemaService.findAll());
    }

    @PostMapping
    public ResponseEntity<Void> createCinema(@Valid @RequestBody CreateCinemaRequest request) {
        cinemaService.createCinema(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{cinemaId}")
    public ResponseEntity<Void> updateCinema(@PathVariable UUID cinemaId, @Valid @RequestBody UpdateCinemaRequest request)
            throws ResourceNotFoundException {
        cinemaService.updateCinema(cinemaId, request);
        return ResponseEntity.noContent().build();
    }
}
