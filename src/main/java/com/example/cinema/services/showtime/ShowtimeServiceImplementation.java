package com.example.cinema.services.showtime;

import com.example.cinema.dtos.showtime.request.CreateShowtimeRequest;
import com.example.cinema.dtos.showtime.request.UpdateShowtimeRequest;
import com.example.cinema.dtos.showtime.response.ShowtimeByTheaterResponse;
import com.example.cinema.dtos.showtime.response.ShowtimeResponse;
import com.example.cinema.events.showtimes.ShowtimeCreatedEvent;
import com.example.cinema.events.showtimes.ShowtimeUpdatedEvent;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.kafka.CinemaEventProducer;
import com.example.cinema.models.showtime.Showtime;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.VersionType;
import com.example.cinema.repositories.showtime.ShowtimeRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import com.example.cinema.services.showtime.inteface.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ShowtimeServiceImplementation implements ShowtimeService {

    private static final int ALERT_MINUTES_BEFORE = 30;

    private final ShowtimeRepository showtimeRepository;
    private final TheaterRepository theaterRepository;
    private final CinemaEventProducer eventProducer;

    @Autowired
    public ShowtimeServiceImplementation(ShowtimeRepository showtimeRepository, TheaterRepository theaterRepository, CinemaEventProducer eventProducer) {
        this.showtimeRepository = showtimeRepository;
        this.theaterRepository = theaterRepository;
        this.eventProducer = eventProducer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createShowtime(CreateShowtimeRequest dto)
            throws ResourceNotFoundException, ConflictException {
        Theater theater = theaterRepository.findById(dto.getTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id: " + dto.getTheaterId()));

        if (showtimeRepository.existsOverlap(dto.getTheaterId(), dto.getDateShowtime(), dto.getStartShowtime(), dto.getEndShowtime(), null)) {
            throw new ConflictException("El horario se traslapa con una función existente en la sala");
        }

        Showtime createdShowtime = showtimeRepository.save(dto.createEntity(theater));

        // Publicar evento de creacion de funcion
        ShowtimeCreatedEvent event = ShowtimeCreatedEvent.fromEntity(createdShowtime);
        eventProducer.publishFunctionCreated(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShowtime(UUID showtimeId, UpdateShowtimeRequest dto)
            throws ResourceNotFoundException, ConflictException {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Función no encontrada con id: " + showtimeId));

        showtime.setMovieId(dto.getMovieId());
        showtime.setDateShowtime(dto.getDateShowtime());
        showtime.setStartShowtime(dto.getStartShowtime());
        showtime.setEndShowtime(dto.getEndShowtime());
        showtime.setVersionType(dto.getVersionType());

        LocalTime start = showtime.getStartShowtime();
        LocalTime end = showtime.getEndShowtime();
        if (!end.isAfter(start)) {
            throw new ConflictException("La hora de fin debe ser posterior a la hora de inicio");
        }

        if (showtimeRepository.existsOverlap(showtime.getTheater().getId(), showtime.getDateShowtime(), start, end, showtimeId)) {
            throw new ConflictException("El horario se traslapa con una función existente en la sala");
        }
        showtimeRepository.save(showtime);

        // Publicar evento de actualizacion de funcion
        ShowtimeUpdatedEvent event = ShowtimeUpdatedEvent.fromEntity(showtime);
        eventProducer.publishFunctionUpdated(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeByTheaterResponse> findShowtimesByTheater(UUID theaterId) {
        List<Showtime> showtimes = showtimeRepository.findByTheater_IdAndIsActiveTrueOrderByDateShowtimeAscStartShowtimeAsc(theaterId);
        LocalDateTime now = LocalDateTime.now();

        List<ShowtimeByTheaterResponse> result = new ArrayList<>();
        for (Showtime showtime : showtimes) {
            LocalDateTime showtimeStart = LocalDateTime.of(showtime.getDateShowtime(), showtime.getStartShowtime());
            if (!showtimeStart.isAfter(now)) {
                showtime.setActive(false);
                showtimeRepository.save(showtime);
                continue;
            }
            String alert = showtimeStart.isBefore(now.plusMinutes(ALERT_MINUTES_BEFORE)) ? "¡La función está a punto de comenzar!" : null;
            result.add(ShowtimeByTheaterResponse.from(showtime, alert));
        }
        return result;
    }
}
