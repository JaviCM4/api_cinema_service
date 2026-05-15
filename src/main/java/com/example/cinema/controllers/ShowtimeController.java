package com.example.cinema.controllers;

import com.example.cinema.dtos.showtime.request.CreateShowtimeRequest;
import com.example.cinema.dtos.showtime.request.UpdateShowtimeRequest;
import com.example.cinema.dtos.showtime.response.ShowtimeResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.showtime.inteface.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @Autowired
    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @PostMapping
    public ResponseEntity<Void> createShowtime(@Valid @RequestBody CreateShowtimeRequest request)
            throws ResourceNotFoundException {
        showtimeService.createShowtime(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{showtimeId}")
    public ResponseEntity<Void> updateShowtime(@PathVariable UUID showtimeId, @Valid @RequestBody UpdateShowtimeRequest request)
            throws ResourceNotFoundException, ConflictException {
        showtimeService.updateShowtime(showtimeId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ShowtimeResponse>> findShowtimes(@RequestParam(required = false) UUID movieId, @RequestParam(required = false) UUID theaterId, @RequestParam(required = false) UUID versionTypeId) {
        return ResponseEntity.ok(showtimeService.findShowtimes(movieId, theaterId, versionTypeId));
    }
}
