package com.example.cinema.services.room;

import com.example.cinema.dtos.room.request.CreateRatingRequest;
import com.example.cinema.dtos.room.request.UpdateRatingRequest;
import com.example.cinema.dtos.room.response.RatingResponse;
import com.example.cinema.dtos.room.response.RatingSummaryResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;
import com.example.cinema.models.room.RoomRating;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.repositories.room.RoomRatingRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import com.example.cinema.services.room.inteface.RoomRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoomRatingServiceImplementation implements RoomRatingService {

    private final RoomRatingRepository ratingRepository;
    private final TheaterRepository theaterRepository;

    @Autowired
    public RoomRatingServiceImplementation(RoomRatingRepository ratingRepository,
                                           TheaterRepository theaterRepository) {
        this.ratingRepository = ratingRepository;
        this.theaterRepository = theaterRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RatingSummaryResponse createRating(UUID theaterId, CreateRatingRequest dto)
            throws ResourceNotFoundException, RestrictedException, ConflictException {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + theaterId));

        if (!theater.isAllowRatings()) {
            throw new RestrictedException("Ratings are not allowed for this theater");
        }

        if (ratingRepository.findByTheater_IdAndUserId(theaterId, dto.getUserId()).isPresent()) {
            throw new ConflictException("User already rated this theater");
        }

        RoomRating rating = dto.createEntity();
        rating.setTheater(theater);
        ratingRepository.save(rating);

        return buildSummary(theaterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RatingSummaryResponse updateRating(UUID ratingId, UpdateRatingRequest dto)
            throws ResourceNotFoundException {
        RoomRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));

        rating.setScore(dto.getScore());
        ratingRepository.save(rating);

        return buildSummary(rating.getTheater().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryResponse findRatingsByTheater(UUID theaterId) throws ResourceNotFoundException {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Theater not found with id: " + theaterId);
        }
        return buildSummary(theaterId);
    }

    private RatingSummaryResponse buildSummary(UUID theaterId) {
        List<RatingResponse> ratings = ratingRepository.findByTheater_Id(theaterId)
                .stream()
                .map(RatingResponse::from)
                .toList();
        Double average = ratingRepository.findAverageScoreByTheater_Id(theaterId);
        return new RatingSummaryResponse(ratings, average);
    }
}
