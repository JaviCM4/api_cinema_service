package com.example.cinema.services.showtime;

import com.example.cinema.dtos.showtime.request.CreateShowtimeRequest;
import com.example.cinema.dtos.showtime.request.UpdateShowtimeRequest;
import com.example.cinema.dtos.showtime.response.ShowtimeResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.showtime.Showtime;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.VersionType;
import com.example.cinema.repositories.showtime.ShowtimeRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import com.example.cinema.repositories.theater.VersionTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
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
    private static final UUID VERSION_TYPE_ID = UUID.randomUUID();

    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private TheaterRepository theaterRepository;
    @Mock private VersionTypeRepository versionTypeRepository;

    @InjectMocks
    private ShowtimeServiceImplementation showtimeService;

    @Test
    void testCreateShowtime() throws Exception {
        // Arrange
        LocalDate date  = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(14, 0);
        LocalTime end   = LocalTime.of(16, 0);
        CreateShowtimeRequest request = new CreateShowtimeRequest(THEATER_ID, MOVIE_ID, VERSION_TYPE_ID, date, start, end);

        Theater theater = buildTheater();
        VersionType versionType = buildVersionType("IMAX");
        ArgumentCaptor<Showtime> captor = ArgumentCaptor.forClass(Showtime.class);

        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(theater));
        when(versionTypeRepository.findById(VERSION_TYPE_ID)).thenReturn(Optional.of(versionType));
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        showtimeService.createShowtime(request);

        // Assert
        assertAll(
                () -> verify(theaterRepository).findById(THEATER_ID),
                () -> verify(versionTypeRepository).findById(VERSION_TYPE_ID),
                () -> verify(showtimeRepository).save(captor.capture()),
                () -> assertEquals(theater,      captor.getValue().getTheater()),
                () -> assertEquals(MOVIE_ID,     captor.getValue().getMovieId()),
                () -> assertEquals(versionType,  captor.getValue().getVersionType()),
                () -> assertEquals(date,         captor.getValue().getDateShowtime()),
                () -> assertEquals(start,        captor.getValue().getStartShowtime()),
                () -> assertEquals(end,          captor.getValue().getEndShowtime())
        );
    }

    @Test
    void testCreateShowtimeTheaterNotFound() {
        // Arrange
        CreateShowtimeRequest request = new CreateShowtimeRequest(THEATER_ID, MOVIE_ID, VERSION_TYPE_ID,
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
    void testCreateShowtimeVersionTypeNotFound() {
        // Arrange
        CreateShowtimeRequest request = new CreateShowtimeRequest(THEATER_ID, MOVIE_ID, VERSION_TYPE_ID,
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0));

        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(buildTheater()));
        when(versionTypeRepository.findById(VERSION_TYPE_ID)).thenReturn(Optional.empty());

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
        UUID newVersionTypeId = UUID.randomUUID();
        LocalDate newDate = LocalDate.now().plusDays(3);
        LocalTime newStart = LocalTime.of(18, 0);
        LocalTime newEnd = LocalTime.of(20, 0);

        UpdateShowtimeRequest request = new UpdateShowtimeRequest(newMovieId, newVersionTypeId, newDate, newStart, newEnd);

        Showtime existing    = buildShowtime(LocalDate.now().plusDays(2), LocalTime.of(14, 0), LocalTime.of(16, 0));
        VersionType newVType = buildVersionType("3D");
        ArgumentCaptor<Showtime> captor = ArgumentCaptor.forClass(Showtime.class);

        when(showtimeRepository.findById(SHOWTIME_ID)).thenReturn(Optional.of(existing));
        when(versionTypeRepository.findById(newVersionTypeId)).thenReturn(Optional.of(newVType));

        // Act
        showtimeService.updateShowtime(SHOWTIME_ID, request);

        // Assert
        assertAll(
                () -> verify(showtimeRepository).save(captor.capture()),
                () -> assertEquals(newMovieId, captor.getValue().getMovieId()),
                () -> assertEquals(newDate,    captor.getValue().getDateShowtime()),
                () -> assertEquals(newStart,   captor.getValue().getStartShowtime()),
                () -> assertEquals(newEnd,     captor.getValue().getEndShowtime()),
                () -> assertEquals(newVType,   captor.getValue().getVersionType())
        );
    }

    @Test
    void testUpdateShowtimeOnlyMovieId() throws Exception {
        // Arrange
        UUID newMovieId = UUID.randomUUID();
        UpdateShowtimeRequest request = new UpdateShowtimeRequest(newMovieId, null, null, null, null);

        Showtime existing = buildShowtime(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0));
        ArgumentCaptor<Showtime> captor = ArgumentCaptor.forClass(Showtime.class);

        when(showtimeRepository.findById(SHOWTIME_ID)).thenReturn(Optional.of(existing));

        // Act
        showtimeService.updateShowtime(SHOWTIME_ID, request);

        // Assert
        assertAll(
                () -> verify(showtimeRepository).save(captor.capture()),
                () -> assertEquals(newMovieId, captor.getValue().getMovieId()),
                () -> verify(versionTypeRepository, never()).findById(any())
        );
    }

    @Test
    void testUpdateShowtimeTimeConflict() {
        // Arrange — se actualiza endShowtime a 08:00 pero startShowtime existente es 10:00
        UpdateShowtimeRequest request = new UpdateShowtimeRequest(null, null, null, null, LocalTime.of(8, 0));

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
        UpdateShowtimeRequest request = new UpdateShowtimeRequest(null, null, null, null, null);

        when(showtimeRepository.findById(SHOWTIME_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> showtimeService.updateShowtime(SHOWTIME_ID, request)),
                () -> verify(showtimeRepository, never()).save(any())
        );
    }

    @Test
    void testUpdateShowtimeVersionTypeNotFound() {
        // Arrange
        UUID newVersionTypeId = UUID.randomUUID();
        UpdateShowtimeRequest request = new UpdateShowtimeRequest(null, newVersionTypeId, null, null, null);

        Showtime existing = buildShowtime(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0));

        when(showtimeRepository.findById(SHOWTIME_ID)).thenReturn(Optional.of(existing));
        when(versionTypeRepository.findById(newVersionTypeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> showtimeService.updateShowtime(SHOWTIME_ID, request)),
                () -> verify(showtimeRepository, never()).save(any())
        );
    }

    @Test
    void testFindShowtimes() {
        // Arrange — función mañana, sin alerta
        Showtime showtime = buildShowtime(
                LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0));

        when(showtimeRepository.findByFilters(MOVIE_ID, null, null))
                .thenReturn(List.of(showtime));

        // Act
        List<ShowtimeResponse> result = showtimeService.findShowtimes(MOVIE_ID, null, null);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertNull(result.get(0).getAlert())
        );
    }

    @Test
    void testFindShowtimesWithAlert() {
        // Arrange — función empieza en 10 minutos (dentro del umbral de 30 min)
        LocalTime start = LocalTime.now().plusMinutes(10);
        LocalTime end   = LocalTime.now().plusMinutes(120);
        Showtime showtime = buildShowtime(LocalDate.now(), start, end);

        when(showtimeRepository.findByFilters(null, THEATER_ID, null))
                .thenReturn(List.of(showtime));

        // Act
        List<ShowtimeResponse> result = showtimeService.findShowtimes(null, THEATER_ID, null);

        // Assert
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertNotNull(result.get(0).getAlert())
        );
    }

    @Test
    void testFindShowtimesEndedShowtime() {
        // Arrange — función terminó ayer → se marca inactiva y no aparece en el resultado
        Showtime ended = buildShowtime(
                LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0));

        when(showtimeRepository.findByFilters(null, null, null))
                .thenReturn(List.of(ended));

        // Act
        List<ShowtimeResponse> result = showtimeService.findShowtimes(null, null, null);

        // Assert
        assertAll(
                () -> assertTrue(result.isEmpty()),
                () -> assertFalse(ended.isActive()),
                () -> verify(showtimeRepository).save(ended)
        );
    }

    @Test
    void testFindShowtimesEmpty() {
        // Arrange
        when(showtimeRepository.findByFilters(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        List<ShowtimeResponse> result = showtimeService.findShowtimes(null, null, null);

        // Assert
        assertTrue(result.isEmpty());
    }

    private Showtime buildShowtime(LocalDate date, LocalTime start, LocalTime end) {
        Showtime s = new Showtime();
        s.setId(SHOWTIME_ID);
        s.setTheater(buildTheater());
        s.setMovieId(MOVIE_ID);
        s.setVersionType(buildVersionType("IMAX"));
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
        return t;
    }

    private VersionType buildVersionType(String name) {
        VersionType vt = new VersionType();
        vt.setId(VERSION_TYPE_ID);
        vt.setName(name);
        return vt;
    }
}
