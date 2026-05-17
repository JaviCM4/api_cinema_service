package com.example.cinema.services.showtime;

import com.example.cinema.dtos.showtime.request.CreateShowtimeRequest;
import com.example.cinema.dtos.showtime.request.UpdateShowtimeRequest;
import com.example.cinema.dtos.showtime.response.ShowtimeByTheaterResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.kafka.CinemaEventProducer;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.showtime.Showtime;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.VersionType;
import com.example.cinema.repositories.showtime.ShowtimeRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShowtimeServiceImplTest {

    private static final UUID SHOWTIME_ID = UUID.randomUUID();
    private static final UUID THEATER_ID = UUID.randomUUID();
    private static final UUID MOVIE_ID = UUID.randomUUID();

    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private TheaterRepository theaterRepository;
    @Mock private CinemaEventProducer eventProducer;

    @InjectMocks
    private ShowtimeServiceImplementation showtimeService;

    @Test
    void testCreateShowtime() throws Exception {
        // Arrange
        LocalDate date  = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(14, 0);
        LocalTime end   = LocalTime.of(16, 0);
        CreateShowtimeRequest request = new CreateShowtimeRequest(THEATER_ID, MOVIE_ID, VersionType.ORIGINAL, date, start, end);

        Theater theater = buildTheater();
        ArgumentCaptor<Showtime> captor = ArgumentCaptor.forClass(Showtime.class);

        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(theater));
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        showtimeService.createShowtime(request);

        // Assert
        assertAll(
                () -> verify(theaterRepository).findById(THEATER_ID),
                () -> verify(showtimeRepository).save(captor.capture()),
                () -> assertEquals(theater,              captor.getValue().getTheater()),
                () -> assertEquals(MOVIE_ID,             captor.getValue().getMovieId()),
                () -> assertEquals(VersionType.ORIGINAL, captor.getValue().getVersionType()),
                () -> assertEquals(date,                 captor.getValue().getDateShowtime()),
                () -> assertEquals(start,                captor.getValue().getStartShowtime()),
                () -> assertEquals(end,                  captor.getValue().getEndShowtime()),
                () -> verify(eventProducer).publishFunctionCreated(any())
        );
    }

    @Test
    void testCreateShowtimeTheaterNotFound() {
        // Arrange
        CreateShowtimeRequest request = new CreateShowtimeRequest(THEATER_ID, MOVIE_ID, VersionType.ORIGINAL,
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0));

        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> showtimeService.createShowtime(request)),
                () -> verify(showtimeRepository, never()).save(any())
        );
    }

    @Test
    void testUpdateShowtime() throws Exception {
        // Arrange
        UUID newMovieId = UUID.randomUUID();
        LocalDate newDate = LocalDate.now().plusDays(3);
        LocalTime newStart = LocalTime.of(18, 0);
        LocalTime newEnd = LocalTime.of(20, 0);

        UpdateShowtimeRequest request = new UpdateShowtimeRequest(newMovieId, VersionType.DUBBED, newDate, newStart, newEnd);

        Showtime existing = buildShowtime(LocalDate.now().plusDays(2), LocalTime.of(14, 0), LocalTime.of(16, 0));
        ArgumentCaptor<Showtime> captor = ArgumentCaptor.forClass(Showtime.class);

        when(showtimeRepository.findById(SHOWTIME_ID)).thenReturn(Optional.of(existing));

        // Act
        showtimeService.updateShowtime(SHOWTIME_ID, request);

        // Assert
        assertAll(
                () -> verify(showtimeRepository).save(captor.capture()),
                () -> assertEquals(newMovieId,        captor.getValue().getMovieId()),
                () -> assertEquals(newDate,           captor.getValue().getDateShowtime()),
                () -> assertEquals(newStart,          captor.getValue().getStartShowtime()),
                () -> assertEquals(newEnd,            captor.getValue().getEndShowtime()),
                () -> assertEquals(VersionType.DUBBED, captor.getValue().getVersionType()),
                () -> verify(eventProducer).publishFunctionUpdated(any())
        );
    }

    @Test
    void testUpdateShowtimeOnlyMovieId() throws Exception {
        // Arrange — solo cambia movieId; los demás campos obligatorios se reenvían iguales
        UUID newMovieId = UUID.randomUUID();
        UpdateShowtimeRequest request = new UpdateShowtimeRequest(
                newMovieId, VersionType.ORIGINAL, null, LocalTime.of(10, 0), LocalTime.of(12, 0));

        Showtime existing = buildShowtime(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0));
        ArgumentCaptor<Showtime> captor = ArgumentCaptor.forClass(Showtime.class);

        when(showtimeRepository.findById(SHOWTIME_ID)).thenReturn(Optional.of(existing));

        // Act
        showtimeService.updateShowtime(SHOWTIME_ID, request);

        // Assert
        assertAll(
                () -> verify(showtimeRepository).save(captor.capture()),
                () -> assertEquals(newMovieId, captor.getValue().getMovieId())
        );
    }

    @Test
    void testUpdateShowtimeTimeConflict() {
        // Arrange — endShowtime anterior a startShowtime
        UpdateShowtimeRequest request = new UpdateShowtimeRequest(
                MOVIE_ID, VersionType.ORIGINAL, null, LocalTime.of(10, 0), LocalTime.of(8, 0));

        Showtime existing = buildShowtime(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0));

        when(showtimeRepository.findById(SHOWTIME_ID)).thenReturn(Optional.of(existing));

        // Act & Assert
        assertAll(
                () -> assertThrows(ConflictException.class,
                        () -> showtimeService.updateShowtime(SHOWTIME_ID, request)),
                () -> verify(showtimeRepository, never()).save(any())
        );
    }

    @Test
    void testUpdateShowtimeNotFound() {
        // Arrange
        UpdateShowtimeRequest request = new UpdateShowtimeRequest(
                MOVIE_ID, VersionType.ORIGINAL, null, LocalTime.of(10, 0), LocalTime.of(12, 0));

        when(showtimeRepository.findById(SHOWTIME_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> showtimeService.updateShowtime(SHOWTIME_ID, request)),
                () -> verify(showtimeRepository, never()).save(any())
        );
    }

    @Test
    void testFindShowtimesByTheater() {
        // Arrange — función mañana, sin alerta
        Showtime showtime = buildShowtime(
                LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0));

        when(showtimeRepository.findByTheater_IdAndIsActiveTrueOrderByDateShowtimeAscStartShowtimeAsc(THEATER_ID))
                .thenReturn(List.of(showtime));

        // Act
        List<ShowtimeByTheaterResponse> result = showtimeService.findShowtimesByTheater(THEATER_ID);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(MOVIE_ID, result.get(0).getMovieId()),
                () -> assertNull(result.get(0).getAlert())
        );
    }

    @Test
    void testFindShowtimesByTheaterWithAlert() {
        // Arrange — función empieza en 10 minutos (dentro del umbral de 30 min)
        LocalTime start = LocalTime.now().plusMinutes(10);
        LocalTime end   = start.plusMinutes(120);
        Showtime showtime = buildShowtime(LocalDate.now(), start, end);

        when(showtimeRepository.findByTheater_IdAndIsActiveTrueOrderByDateShowtimeAscStartShowtimeAsc(THEATER_ID))
                .thenReturn(List.of(showtime));

        // Act
        List<ShowtimeByTheaterResponse> result = showtimeService.findShowtimesByTheater(THEATER_ID);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertNotNull(result.get(0).getAlert())
        );
    }

    @Test
    void testFindShowtimesByTheaterPastShowtime() {
        // Arrange — función ya pasó → se marca inactiva y no aparece
        Showtime ended = buildShowtime(
                LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0));

        when(showtimeRepository.findByTheater_IdAndIsActiveTrueOrderByDateShowtimeAscStartShowtimeAsc(THEATER_ID))
                .thenReturn(List.of(ended));

        // Act
        List<ShowtimeByTheaterResponse> result = showtimeService.findShowtimesByTheater(THEATER_ID);

        // Assert
        assertAll(
                () -> assertTrue(result.isEmpty()),
                () -> assertFalse(ended.isActive()),
                () -> verify(showtimeRepository).save(ended)
        );
    }

    @Test
    void testFindShowtimesByTheaterEmpty() {
        // Arrange
        when(showtimeRepository.findByTheater_IdAndIsActiveTrueOrderByDateShowtimeAscStartShowtimeAsc(THEATER_ID))
                .thenReturn(List.of());

        // Act
        List<ShowtimeByTheaterResponse> result = showtimeService.findShowtimesByTheater(THEATER_ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    private Showtime buildShowtime(LocalDate date, LocalTime start, LocalTime end) {
        Showtime s = new Showtime();
        s.setId(SHOWTIME_ID);
        s.setTheater(buildTheater());
        s.setMovieId(MOVIE_ID);
        s.setVersionType(VersionType.ORIGINAL);
        s.setDateShowtime(date);
        s.setStartShowtime(start);
        s.setEndShowtime(end);
        s.setActive(true);
        return s;
    }

    private Theater buildTheater() {
        Theater t = new Theater();
        t.setId(THEATER_ID);
        t.setName("Sala 1");
        t.setCinema(buildCinema());
        return t;
    }

    private Cinema buildCinema() {
        Cinema c = new Cinema();
        c.setId(UUID.randomUUID());
        c.setName("Cinepolis");
        return c;
    }
}
