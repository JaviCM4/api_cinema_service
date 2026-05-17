package com.example.cinema.services.room;

import com.example.cinema.dtos.room.request.CreateRatingRequest;
import com.example.cinema.dtos.room.request.UpdateRatingRequest;
import com.example.cinema.dtos.room.response.RatingSummaryResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;
import com.example.cinema.kafka.CinemaEventProducer;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.room.RoomRating;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.repositories.room.RoomRatingRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomRatingServiceImplTest {

    private static final UUID THEATER_ID = UUID.randomUUID();
    private static final UUID RATING_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock private RoomRatingRepository ratingRepository;
    @Mock private TheaterRepository theaterRepository;
    @Mock private CinemaEventProducer eventProducer;

    @InjectMocks
    private RoomRatingServiceImplementation ratingService;

    @Test
    void testCreateRating() throws Exception {
        // Arrange
        CreateRatingRequest request = new CreateRatingRequest(USER_ID, (short) 4);
        Theater theater = buildTheater(true);
        RoomRating saved = buildRating((short) 4);

        ArgumentCaptor<RoomRating> captor = ArgumentCaptor.forClass(RoomRating.class);

        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(theater));
        when(ratingRepository.findByTheater_IdAndUserId(THEATER_ID, USER_ID)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(RoomRating.class))).thenReturn(saved);

        // Act
        ratingService.createRating(THEATER_ID, request);

        // Assert
        assertAll(
                () -> verify(ratingRepository).save(captor.capture()),
                () -> assertEquals(theater,   captor.getValue().getTheater()),
                () -> assertEquals(USER_ID,   captor.getValue().getUserId()),
                () -> assertEquals((short) 4, captor.getValue().getScore())
        );
    }

    @Test
    void testCreateRatingTheaterNotFound() {
        // Arrange
        CreateRatingRequest request = new CreateRatingRequest(USER_ID, (short) 4);
        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> ratingService.createRating(THEATER_ID, request));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void testCreateRatingWhenRatingsNotAllowed() {
        // Arrange
        CreateRatingRequest request = new CreateRatingRequest(USER_ID, (short) 4);
        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(buildTheater(false)));

        // Assert
        assertThrows(RestrictedException.class,
                () -> ratingService.createRating(THEATER_ID, request));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void testCreateRatingDuplicate() {
        // Arrange
        CreateRatingRequest request = new CreateRatingRequest(USER_ID, (short) 4);
        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(buildTheater(true)));
        when(ratingRepository.findByTheater_IdAndUserId(THEATER_ID, USER_ID))
                .thenReturn(Optional.of(buildRating((short) 3)));

        // Assert
        assertThrows(ConflictException.class,
                () -> ratingService.createRating(THEATER_ID, request));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void testUpdateRating() throws Exception {
        // Arrange
        UpdateRatingRequest request        = new UpdateRatingRequest(USER_ID, (short) 5);
        Theater theater                    = buildTheater(true);
        RoomRating existing                = buildRating((short) 3);
        existing.setTheater(theater);
        ArgumentCaptor<RoomRating> captor  = ArgumentCaptor.forClass(RoomRating.class);

        when(ratingRepository.findById(RATING_ID)).thenReturn(Optional.of(existing));
        when(ratingRepository.save(any(RoomRating.class))).thenReturn(existing);

        // Act
        ratingService.updateRating(RATING_ID, request);

        // Assert
        assertAll(
                () -> verify(ratingRepository).save(captor.capture()),
                () -> assertEquals((short) 5, captor.getValue().getScore())
        );
    }

    @Test
    void testUpdateRatingWrongUser() {
        // Arrange
        UpdateRatingRequest request = new UpdateRatingRequest(UUID.randomUUID(), (short) 5);
        RoomRating existing        = buildRating((short) 3);

        when(ratingRepository.findById(RATING_ID)).thenReturn(Optional.of(existing));

        // Assert
        assertThrows(ConflictException.class,
                () -> ratingService.updateRating(RATING_ID, request));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void testUpdateRatingNotFound() {
        // Arrange
        UpdateRatingRequest request = new UpdateRatingRequest(USER_ID, (short) 5);
        when(ratingRepository.findById(RATING_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> ratingService.updateRating(RATING_ID, request));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void testFindRatingsByTheater() throws Exception {
        // Arrange
        RoomRating r1 = buildRating((short) 4);
        RoomRating r2 = buildRating((short) 5);
        r2.setId(UUID.randomUUID());

        when(theaterRepository.existsById(THEATER_ID)).thenReturn(true);
        when(ratingRepository.findByTheater_Id(THEATER_ID)).thenReturn(List.of(r1, r2));
        when(ratingRepository.findAverageScoreByTheater_Id(THEATER_ID)).thenReturn(4.5);

        // Act
        RatingSummaryResponse result = ratingService.findRatingsByTheater(THEATER_ID);

        // Assert
        assertAll(
                () -> assertEquals(2,   result.getRatings().size()),
                () -> assertEquals(4.5, result.getAverageScore())
        );
    }

    @Test
    void testFindRatingsByTheaterEmpty() throws Exception {
        // Arrange
        when(theaterRepository.existsById(THEATER_ID)).thenReturn(true);
        when(ratingRepository.findByTheater_Id(THEATER_ID)).thenReturn(List.of());
        when(ratingRepository.findAverageScoreByTheater_Id(THEATER_ID)).thenReturn(null);

        // Act
        RatingSummaryResponse result = ratingService.findRatingsByTheater(THEATER_ID);

        // Assert
        assertAll(
                () -> assertTrue(result.getRatings().isEmpty()),
                () -> assertNull(result.getAverageScore())
        );
    }

    @Test
    void testFindRatingsByTheaterNotFound() {
        // Arrange
        when(theaterRepository.existsById(THEATER_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> ratingService.findRatingsByTheater(THEATER_ID));
        verify(ratingRepository, never()).findByTheater_Id(any());
    }

    private Theater buildTheater(boolean allowRatings) {
        Theater theater = new Theater();
        theater.setId(THEATER_ID);
        theater.setName("Sala 1");
        theater.setAllowRatings(allowRatings);
        theater.setCinema(buildCinema());
        return theater;
    }

    private Cinema buildCinema() {
        Cinema cinema = new Cinema();
        cinema.setId(UUID.randomUUID());
        cinema.setName("Cinepolis");
        return cinema;
    }

    private RoomRating buildRating(short score) {
        RoomRating rating = new RoomRating();
        rating.setId(RATING_ID);
        rating.setUserId(USER_ID);
        rating.setScore(score);
        rating.setCreatedAt(LocalDateTime.now());
        return rating;
    }
}
