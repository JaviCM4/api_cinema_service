package com.example.cinema.services.theater;

import com.example.cinema.dtos.theater.request.CreateTheaterRequest;
import com.example.cinema.dtos.theater.request.UpdateTheaterRequest;
import com.example.cinema.dtos.theater.response.TheaterResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.theater.Seat;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.TypeTheater;
import com.example.cinema.repositories.cinema.CinemaRepository;
import com.example.cinema.repositories.theater.SeatRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import com.example.cinema.repositories.theater.TypeTheaterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TheaterServiceImplTest {

    private static final UUID THEATER_ID = UUID.randomUUID();
    private static final UUID CINEMA_ID = UUID.randomUUID();
    private static final UUID TYPE_THEATER_ID = UUID.randomUUID();

    @Mock private TheaterRepository theaterRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private CinemaRepository cinemaRepository;
    @Mock private TypeTheaterRepository typeTheaterRepository;

    @InjectMocks
    private TheaterServiceImplementation theaterService;

    @Test
    void testCreateTheater() throws Exception {
        // Arrange
        CreateTheaterRequest request = new CreateTheaterRequest(CINEMA_ID, TYPE_THEATER_ID, "Sala 1", 5, 10);
        Cinema cinema = buildCinema();
        TypeTheater typeTheater = buildTypeTheater("IMAX");
        Theater savedTheater  = buildTheater("Sala 1", cinema, typeTheater);

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(cinema));
        when(typeTheaterRepository.findById(TYPE_THEATER_ID)).thenReturn(Optional.of(typeTheater));
        when(theaterRepository.existsByCinema_IdAndNameIgnoreCase(CINEMA_ID, "Sala 1")).thenReturn(false);
        when(theaterRepository.save(any(Theater.class))).thenReturn(savedTheater);

        // Act
        theaterService.createTheater(request);

        // Assert
        assertAll(
                () -> verify(cinemaRepository).findById(CINEMA_ID),
                () -> verify(typeTheaterRepository).findById(TYPE_THEATER_ID),
                () -> verify(theaterRepository).existsByCinema_IdAndNameIgnoreCase(CINEMA_ID, "Sala 1"),
                () -> verify(theaterRepository).save(any(Theater.class)),
                () -> verify(seatRepository).saveAll(argThat(list -> ((Collection<?>) list).size() == 50))
        );
    }

    @Test
    void testCreateTheaterCinemaNotFound() {
        // Arrange
        CreateTheaterRequest request = new CreateTheaterRequest(CINEMA_ID, TYPE_THEATER_ID, "Sala 1", 5, 10);
        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> theaterService.createTheater(request));
        verify(theaterRepository, never()).save(any());
        verify(seatRepository, never()).saveAll(any());
    }

    @Test
    void testCreateTheaterTypeTheaterNotFound() {
        // Arrange
        CreateTheaterRequest request = new CreateTheaterRequest(CINEMA_ID, TYPE_THEATER_ID, "Sala 1", 5, 10);
        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(buildCinema()));
        when(typeTheaterRepository.findById(TYPE_THEATER_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> theaterService.createTheater(request));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void testCreateTheaterNameConflict() {
        // Arrange
        CreateTheaterRequest request = new CreateTheaterRequest(CINEMA_ID, TYPE_THEATER_ID, "Sala 1", 5, 10);
        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(buildCinema()));
        when(typeTheaterRepository.findById(TYPE_THEATER_ID)).thenReturn(Optional.of(buildTypeTheater("2D")));
        when(theaterRepository.existsByCinema_IdAndNameIgnoreCase(CINEMA_ID, "Sala 1")).thenReturn(true);

        // Assert
        assertThrows(ConflictException.class, () -> theaterService.createTheater(request));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void testUpdateTheater() throws Exception {
        // Arrange
        UUID newTypeId   = UUID.randomUUID();
        TypeTheater newType = buildTypeTheater("3D");
        newType.setId(newTypeId);

        UpdateTheaterRequest request = new UpdateTheaterRequest(newTypeId, "Sala Actualizada", false, true, false);
        Theater existing = buildTheater("Sala 1", buildCinema(), buildTypeTheater("2D"));

        ArgumentCaptor<Theater> captor = ArgumentCaptor.forClass(Theater.class);

        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(existing));
        when(typeTheaterRepository.findById(newTypeId)).thenReturn(Optional.of(newType));
        when(theaterRepository.save(any(Theater.class))).thenReturn(existing);

        // Act
        theaterService.updateTheater(THEATER_ID, request);

        // Assert
        assertAll(
                () -> verify(theaterRepository).save(captor.capture()),
                () -> assertEquals("Sala Actualizada", captor.getValue().getName()),
                () -> assertEquals(newType,             captor.getValue().getTypeTheater()),
                () -> assertFalse(captor.getValue().isVisible()),
                () -> assertTrue(captor.getValue().isAllowComments()),
                () -> assertFalse(captor.getValue().isAllowRatings())
        );
    }

    @Test
    void testUpdateTheaterOnlyNonNullFields() throws Exception {
        // Arrange
        UpdateTheaterRequest request = new UpdateTheaterRequest(null, "Sala B", null, null, null);
        TypeTheater originalType = buildTypeTheater("2D");
        Theater existing = buildTheater("Sala A", buildCinema(), originalType);
        existing.setVisible(false);

        ArgumentCaptor<Theater> captor = ArgumentCaptor.forClass(Theater.class);

        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(existing));
        when(theaterRepository.save(any(Theater.class))).thenReturn(existing);

        // Act
        theaterService.updateTheater(THEATER_ID, request);

        // Assert
        assertAll(
                () -> verify(theaterRepository).save(captor.capture()),
                () -> assertEquals("Sala B",    captor.getValue().getName()),
                () -> assertEquals(originalType, captor.getValue().getTypeTheater()),
                () -> assertFalse(captor.getValue().isVisible())
        );
    }

    @Test
    void testUpdateTheaterNotFound() {
        // Arrange
        UpdateTheaterRequest request = new UpdateTheaterRequest(null, "Sala B", null, null, null);
        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> theaterService.updateTheater(THEATER_ID, request));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void testUpdateTheaterTypeNotFound() {
        // Arrange
        UUID newTypeId = UUID.randomUUID();
        UpdateTheaterRequest request = new UpdateTheaterRequest(newTypeId, null, null, null, null);

        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(buildTheater("Sala 1", buildCinema(), buildTypeTheater("2D"))));
        when(typeTheaterRepository.findById(newTypeId)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> theaterService.updateTheater(THEATER_ID, request));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void testGetTheater() throws Exception {
        // Arrange
        Cinema cinema    = buildCinema();
        TypeTheater type = buildTypeTheater("IMAX");
        Theater theater  = buildTheater("Sala IMAX", cinema, type);
        Seat seat1       = buildSeat(theater, "A", 1);
        Seat seat2       = buildSeat(theater, "A", 2);

        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(theater));
        when(seatRepository.findByTheater_Id(THEATER_ID)).thenReturn(List.of(seat1, seat2));

        // Act
        TheaterResponse result = theaterService.getTheater(THEATER_ID);

        // Assert
        assertAll(
                () -> assertEquals(THEATER_ID,  result.getId()),
                () -> assertEquals("Sala IMAX", result.getName()),
                () -> assertEquals(2,            result.getSeats().size()),
                () -> assertEquals("A",          result.getSeats().get(0).getRowName()),
                () -> assertEquals(1,            result.getSeats().get(0).getColNumber())
        );
    }

    @Test
    void testGetTheaterNotFound() {
        // Arrange
        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> theaterService.getTheater(THEATER_ID));
        verify(seatRepository, never()).findByTheater_Id(any());
    }

    private Cinema buildCinema() {
        Cinema cinema = new Cinema();
        cinema.setId(CINEMA_ID);
        cinema.setName("Cinepolis Centro");
        return cinema;
    }

    private TypeTheater buildTypeTheater(String name) {
        TypeTheater t = new TypeTheater();
        t.setId(TYPE_THEATER_ID);
        t.setName(name);
        return t;
    }

    private Theater buildTheater(String name, Cinema cinema, TypeTheater typeTheater) {
        Theater theater = new Theater();
        theater.setId(THEATER_ID);
        theater.setCinema(cinema);
        theater.setTypeTheater(typeTheater);
        theater.setName(name);
        theater.setRows(5);
        theater.setCols(10);
        theater.setVisible(true);
        theater.setAllowComments(true);
        theater.setAllowRatings(true);
        theater.setCreatedAt(LocalDateTime.now());
        theater.setUpdatedAt(LocalDateTime.now());
        return theater;
    }

    private Seat buildSeat(Theater theater, String rowName, int colNumber) {
        Seat seat = new Seat();
        seat.setId(UUID.randomUUID());
        seat.setTheater(theater);
        seat.setRowName(rowName);
        seat.setColNumber(colNumber);
        seat.setActive(true);
        return seat;
    }
}
