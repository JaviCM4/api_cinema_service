package com.example.cinema.services.theater;

import com.example.cinema.dtos.theater.response.SeatResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.theater.Seat;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.repositories.theater.SeatRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatServiceImplTest {

    private static final UUID THEATER_ID = UUID.randomUUID();
    private static final UUID SEAT_ID    = UUID.randomUUID();

    @Mock private SeatRepository    seatRepository;
    @Mock private TheaterRepository theaterRepository;

    @InjectMocks
    private SeatServiceImplementation seatService;

    // ──────────────────────────────── findByTheaterId ────────────────────────────────

    @Test
    void testFindByTheaterId() throws ResourceNotFoundException {
        // Arrange
        Seat seat1 = buildSeat("A", 1, true);
        Seat seat2 = buildSeat("A", 2, false);

        when(theaterRepository.existsById(THEATER_ID)).thenReturn(true);
        when(seatRepository.findByTheater_IdOrderByRowNameAscColNumberAsc(THEATER_ID)).thenReturn(List.of(seat1, seat2));

        // Act
        List<SeatResponse> result = seatService.findByTheaterId(THEATER_ID);

        // Assert
        assertAll(
                () -> assertEquals(2,    result.size()),
                () -> assertEquals("A",  result.get(0).getRowName()),
                () -> assertEquals(1,    result.get(0).getColNumber()),
                () -> assertTrue(result.get(0).isActive()),
                () -> assertFalse(result.get(1).isActive())
        );
    }

    @Test
    void testFindByTheaterIdEmpty() throws ResourceNotFoundException {
        // Arrange
        when(theaterRepository.existsById(THEATER_ID)).thenReturn(true);
        when(seatRepository.findByTheater_IdOrderByRowNameAscColNumberAsc(THEATER_ID)).thenReturn(List.of());

        // Act
        List<SeatResponse> result = seatService.findByTheaterId(THEATER_ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByTheaterIdNotFound() {
        // Arrange
        when(theaterRepository.existsById(THEATER_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> seatService.findByTheaterId(THEATER_ID));
        verify(seatRepository, never()).findByTheater_IdOrderByRowNameAscColNumberAsc(any());
    }

    // ──────────────────────────────── toggleSeatStatus ───────────────────────────────

    @Test
    void testToggleSeatActiveToInactive() throws ResourceNotFoundException {
        // Arrange — asiento activo → debe quedar inactivo
        Seat seat = buildSeat("B", 3, true);
        ArgumentCaptor<Seat> captor = ArgumentCaptor.forClass(Seat.class);

        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);

        // Act
        seatService.toggleSeatStatus(SEAT_ID);

        // Assert
        assertAll(
                () -> verify(seatRepository).save(captor.capture()),
                () -> assertFalse(captor.getValue().isActive())
        );
    }

    @Test
    void testToggleSeatInactiveToActive() throws ResourceNotFoundException {
        // Arrange — asiento inactivo → debe quedar activo
        Seat seat = buildSeat("C", 5, false);
        ArgumentCaptor<Seat> captor = ArgumentCaptor.forClass(Seat.class);

        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);

        // Act
        seatService.toggleSeatStatus(SEAT_ID);

        // Assert
        assertAll(
                () -> verify(seatRepository).save(captor.capture()),
                () -> assertTrue(captor.getValue().isActive())
        );
    }

    @Test
    void testToggleSeatNotFound() {
        // Arrange
        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> seatService.toggleSeatStatus(SEAT_ID));
        verify(seatRepository, never()).save(any());
    }

    // ──────────────────────────────── helpers ────────────────────────────────────────

    private Seat buildSeat(String rowName, int colNumber, boolean active) {
        Theater theater = new Theater();
        theater.setId(THEATER_ID);

        Seat seat = new Seat();
        seat.setId(SEAT_ID);
        seat.setTheater(theater);
        seat.setRowName(rowName);
        seat.setColNumber(colNumber);
        seat.setActive(active);
        return seat;
    }
}
