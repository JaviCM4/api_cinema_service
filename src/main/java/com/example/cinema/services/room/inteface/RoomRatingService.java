package com.example.cinema.services.room.inteface;

import com.example.cinema.dtos.room.request.CreateRatingRequest;
import com.example.cinema.dtos.room.request.UpdateRatingRequest;
import com.example.cinema.dtos.room.response.RatingSummaryResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;

import java.util.UUID;

public interface RoomRatingService {

    void createRating(UUID theaterId, CreateRatingRequest dto)
            throws ResourceNotFoundException, RestrictedException, ConflictException;

    void updateRating(UUID ratingId, UpdateRatingRequest dto)
            throws ResourceNotFoundException, ConflictException;

    RatingSummaryResponse findRatingsByTheater(UUID theaterId) throws ResourceNotFoundException;
}
