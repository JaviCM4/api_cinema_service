package com.example.cinema.services.room;

import com.example.cinema.client.tickets.TicketsClient;
import com.example.cinema.dtos.room.request.CreateRatingRequest;
import com.example.cinema.dtos.room.request.UpdateRatingRequest;
import com.example.cinema.dtos.room.response.RatingResponse;
import com.example.cinema.dtos.room.response.RatingSummaryResponse;
import com.example.cinema.events.ratings.RoomRatingCreatedEvent;
import com.example.cinema.events.ratings.RoomRatingUpdatedEvent;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;
import com.example.cinema.kafka.CinemaEventProducer;
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
    private final CinemaEventProducer eventProducer;
    private final TicketsClient ticketsClient;

    @Autowired
    public RoomRatingServiceImplementation(RoomRatingRepository ratingRepository, TheaterRepository theaterRepository, CinemaEventProducer eventProducer, TicketsClient ticketsClient) {
        this.ratingRepository = ratingRepository;
        this.theaterRepository = theaterRepository;
        this.eventProducer = eventProducer;
        this.ticketsClient = ticketsClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRating(UUID theaterId, CreateRatingRequest dto)
            throws ResourceNotFoundException, RestrictedException, ConflictException {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id: " + theaterId));

        if (!theater.isAllowRatings()) {
            throw new RestrictedException("Las calificaciones no están permitidas en esta sala");
        }

        if (ratingRepository.findByTheater_IdAndUserId(theaterId, dto.getUserId()).isPresent()) {
            throw new ConflictException("El usuario ya califico esta sala");
        }

        //Verificar que el usuario tenga tickets para esa sala
        if (!ticketsClient.hasTicketsByRoomAndUser(theaterId, dto.getUserId())) {
            throw new RestrictedException("Debes haber comprado al menos un boleto en esta sala para calificarla");
        }

        RoomRating rating = dto.createEntity();
        rating.setTheater(theater);
        ratingRepository.save(rating);
        // Publicar evento de creacion de calificación
        RoomRatingCreatedEvent event = RoomRatingCreatedEvent.fromEntity(rating);
        eventProducer.publishRoomRatingCreated(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRating(UUID ratingId, UpdateRatingRequest dto)
            throws ResourceNotFoundException {
        RoomRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Calificación no encontrada con id: " + ratingId));

        rating.setScore(dto.getScore());
        ratingRepository.save(rating);
        // Publicar evento de actualizacion de calificacion
        RoomRatingUpdatedEvent event = RoomRatingUpdatedEvent.fromEntity(rating.getId(), rating.getScore());
        eventProducer.publishRoomRatingUpdated(event);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryResponse findRatingsByTheater(UUID theaterId) throws ResourceNotFoundException {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Sala no encontrada con id: " + theaterId);
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
