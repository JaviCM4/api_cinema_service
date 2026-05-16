package com.example.cinema.services.showtime;

import com.example.cinema.dtos.showtime.request.CreateShowtimeRequest;
import com.example.cinema.dtos.showtime.request.UpdateShowtimeRequest;
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
import com.example.cinema.repositories.theater.VersionTypeRepository;
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
    private final VersionTypeRepository versionTypeRepository;
    private final CinemaEventProducer eventProducer;

    @Autowired
    public ShowtimeServiceImplementation(ShowtimeRepository showtimeRepository, TheaterRepository theaterRepository, VersionTypeRepository versionTypeRepository, CinemaEventProducer eventProducer) {
        this.showtimeRepository = showtimeRepository;
        this.theaterRepository = theaterRepository;
        this.versionTypeRepository = versionTypeRepository;
        this.eventProducer = eventProducer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createShowtime(CreateShowtimeRequest dto) throws ResourceNotFoundException {
        Theater theater = theaterRepository.findById(dto.getTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id: " + dto.getTheaterId()));

        VersionType versionType = versionTypeRepository.findById(dto.getVersionTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de versión no encontrado con id: " + dto.getVersionTypeId()));

        Showtime createdShowtime = showtimeRepository.save(dto.createEntity(theater, versionType));

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

        if (dto.getMovieId() != null)  showtime.setMovieId(dto.getMovieId());
        if (dto.getDateShowtime() != null) showtime.setDateShowtime(dto.getDateShowtime());
        if (dto.getStartShowtime() != null) showtime.setStartShowtime(dto.getStartShowtime());
        if (dto.getEndShowtime() != null) showtime.setEndShowtime(dto.getEndShowtime());

        if (dto.getVersionTypeId() != null) {
            VersionType versionType = versionTypeRepository.findById(dto.getVersionTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de versión no encontrado con id: " + dto.getVersionTypeId()));
            showtime.setVersionType(versionType);
        }

        LocalTime start = showtime.getStartShowtime();
        LocalTime end = showtime.getEndShowtime();
        if (!end.isAfter(start)) {
            throw new ConflictException("La hora de fin debe ser posterior a la hora de inicio");
        }
        showtimeRepository.save(showtime);

        // Publicar evento de actualizacion de funcion
        ShowtimeUpdatedEvent event = ShowtimeUpdatedEvent.fromEntity(showtime);
        eventProducer.publishFunctionUpdated(event);
    }

    @Override
    @Transactional
    public List<ShowtimeResponse> findShowtimes(UUID movieId, UUID theaterId, UUID versionTypeId) {
        List<Showtime> showtimes = showtimeRepository.findByFilters(movieId, theaterId, versionTypeId);
        LocalDateTime now = LocalDateTime.now();

        List<ShowtimeResponse> result = new ArrayList<>();
        for (Showtime showtime : showtimes) {
            LocalDateTime showtimeEnd = LocalDateTime.of(showtime.getDateShowtime(), showtime.getEndShowtime());

            if (showtimeEnd.isBefore(now)) {
                showtime.setActive(false);
                showtimeRepository.save(showtime);
                continue;
            }

            LocalDateTime showtimeStart = LocalDateTime.of(showtime.getDateShowtime(), showtime.getStartShowtime());
            String alert = null;
            if (showtimeStart.isAfter(now) && !showtimeStart.isAfter(now.plusMinutes(ALERT_MINUTES_BEFORE))) {
                alert = "¡La función está a punto de comenzar!";
            }
            result.add(ShowtimeResponse.from(showtime, alert));
        }
        return result;
    }
}
