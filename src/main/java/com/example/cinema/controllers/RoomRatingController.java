package com.example.cinema.controllers;

import com.example.cinema.dtos.room.request.CreateRatingRequest;
import com.example.cinema.dtos.room.request.UpdateRatingRequest;
import com.example.cinema.dtos.room.response.RatingSummaryResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;
import com.example.cinema.services.room.inteface.RoomRatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/theaters/{theaterId}/ratings")
public class RoomRatingController {

    private final RoomRatingService ratingService;

    @Autowired
    public RoomRatingController(RoomRatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping
    public ResponseEntity<RatingSummaryResponse> getRatings(@PathVariable UUID theaterId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(ratingService.findRatingsByTheater(theaterId));
    }

    @PostMapping
    public ResponseEntity<Void> createRating(@PathVariable UUID theaterId, @Valid @RequestBody CreateRatingRequest request)
            throws ResourceNotFoundException, RestrictedException, ConflictException {
        ratingService.createRating(theaterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{ratingId}")
    public ResponseEntity<Void> updateRating(@PathVariable UUID ratingId, @Valid @RequestBody UpdateRatingRequest request)
            throws ResourceNotFoundException {
        ratingService.updateRating(ratingId, request);
        return ResponseEntity.noContent().build();
    }
}
