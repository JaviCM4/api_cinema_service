package com.example.cinema.services.theater;

import com.example.cinema.dtos.theater.request.CreateTheaterRequest;
import com.example.cinema.dtos.theater.request.UpdateTheaterRequest;
import com.example.cinema.dtos.theater.response.ShowtimeInTheaterResponse;
import com.example.cinema.dtos.theater.response.TheaterClientResponse;
import com.example.cinema.dtos.theater.response.TheaterResponse;
import com.example.cinema.dtos.theater.response.TypeTheaterResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.showtime.Showtime;
import com.example.cinema.models.theater.Seat;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.TypeTheater;
import com.example.cinema.repositories.cinema.CinemaRepository;
import com.example.cinema.repositories.showtime.ShowtimeRepository;
import com.example.cinema.repositories.theater.SeatRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import com.example.cinema.repositories.theater.TypeTheaterRepository;
import com.example.cinema.services.theater.inteface.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TheaterServiceImplementation implements TheaterService {

    private static final int ALERT_MINUTES_BEFORE = 30;

    private final TheaterRepository theaterRepository;
    private final SeatRepository seatRepository;
    private final CinemaRepository cinemaRepository;
    private final TypeTheaterRepository typeTheaterRepository;
    private final ShowtimeRepository showtimeRepository;

    @Autowired
    public TheaterServiceImplementation(TheaterRepository theaterRepository, SeatRepository seatRepository, CinemaRepository cinemaRepository, TypeTheaterRepository typeTheaterRepository, ShowtimeRepository showtimeRepository) {
        this.theaterRepository = theaterRepository;
        this.seatRepository = seatRepository;
        this.cinemaRepository = cinemaRepository;
        this.typeTheaterRepository = typeTheaterRepository;
        this.showtimeRepository = showtimeRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTheater(CreateTheaterRequest dto) throws ResourceNotFoundException, ConflictException {
        Cinema cinema = cinemaRepository.findById(dto.getCinemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cine no encontrado con id: " + dto.getCinemaId()));

        TypeTheater typeTheater = typeTheaterRepository.findById(dto.getTypeTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de sala no encontrado con id: " + dto.getTypeTheaterId()));

        if (theaterRepository.existsByCinema_IdAndNameIgnoreCase(dto.getCinemaId(), dto.getName())) {
            throw new ConflictException("Ya existe una sala con el nombre '" + dto.getName() + "' en este cine");
        }

        Theater saved = theaterRepository.save(dto.createEntity(cinema, typeTheater));

        List<Seat> seats = generateSeats(saved, dto.getRows(), dto.getCols());
        seatRepository.saveAll(seats);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTheater(UUID theaterId, UpdateTheaterRequest dto) throws ResourceNotFoundException {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id: " + theaterId));

        TypeTheater typeTheater = typeTheaterRepository.findById(dto.getTypeTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de sala no encontrado con id: " + dto.getTypeTheaterId()));

        theater.setTypeTheater(typeTheater);
        theater.setName(dto.getName());
        theater.setVisible(dto.getIsVisible());
        theater.setAllowComments(dto.getAllowComments());
        theater.setAllowRatings(dto.getAllowRatings());
        theaterRepository.save(theater);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TheaterResponse> findTheatersByCinema(UUID cinemaId) {
        return theaterRepository.findByCinema_Id(cinemaId)
                .stream()
                .map(TheaterResponse::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TheaterClientResponse> findTheatersByMovie(UUID movieId) {
        List<Showtime> showtimes =
                showtimeRepository.findByMovieIdAndIsActiveTrueOrderByDateShowtimeAscStartShowtimeAsc(movieId);

        LocalDateTime now = LocalDateTime.now();
        Map<UUID, Theater> theaterMap = new LinkedHashMap<>();
        Map<UUID, List<ShowtimeInTheaterResponse>> showtimeMap = new LinkedHashMap<>();

        for (Showtime showtime : showtimes) {
            LocalDateTime start = showtime.getDateShowtime().atTime(showtime.getStartShowtime());
            if (!start.isAfter(now)) {
                showtime.setActive(false);
                showtimeRepository.save(showtime);
                continue;
            }
            String alert = start.isBefore(now.plusMinutes(ALERT_MINUTES_BEFORE))
                    ? "Comienza en menos de " + ALERT_MINUTES_BEFORE + " minutos"
                    : null;

            UUID theaterId = showtime.getTheater().getId();
            theaterMap.putIfAbsent(theaterId, showtime.getTheater());
            showtimeMap.computeIfAbsent(theaterId, k -> new ArrayList<>())
                    .add(ShowtimeInTheaterResponse.from(showtime, alert));
        }

        return theaterMap.entrySet().stream()
                .map(e -> TheaterClientResponse.from(e.getValue(),
                        showtimeMap.getOrDefault(e.getKey(), List.of())))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TheaterClientResponse> findTheatersWithShowtimesByCinema(UUID cinemaId) {
        List<Showtime> showtimes =
                showtimeRepository.findByTheater_Cinema_IdAndIsActiveTrueOrderByDateShowtimeAscStartShowtimeAsc(cinemaId);

        LocalDateTime now = LocalDateTime.now();
        Map<UUID, Theater> theaterMap = new LinkedHashMap<>();
        Map<UUID, List<ShowtimeInTheaterResponse>> showtimeMap = new LinkedHashMap<>();

        for (Showtime showtime : showtimes) {
            LocalDateTime start = showtime.getDateShowtime().atTime(showtime.getStartShowtime());
            if (!start.isAfter(now)) {
                showtime.setActive(false);
                showtimeRepository.save(showtime);
                continue;
            }
            String alert = start.isBefore(now.plusMinutes(ALERT_MINUTES_BEFORE))
                    ? "Comienza en menos de " + ALERT_MINUTES_BEFORE + " minutos"
                    : null;

            UUID theaterId = showtime.getTheater().getId();
            theaterMap.putIfAbsent(theaterId, showtime.getTheater());
            showtimeMap.computeIfAbsent(theaterId, k -> new ArrayList<>())
                    .add(ShowtimeInTheaterResponse.from(showtime, alert));
        }

        return theaterMap.entrySet().stream()
                .map(e -> TheaterClientResponse.from(e.getValue(),
                        showtimeMap.getOrDefault(e.getKey(), List.of())))
                .toList();
    }

    private List<Seat> generateSeats(Theater theater, int rows, int cols) {
        List<Seat> seats = new ArrayList<>(rows * cols);
        for (int r = 1; r <= rows; r++) {
            String rowName = toRowName(r);
            for (int c = 1; c <= cols; c++) {
                Seat seat = new Seat();
                seat.setTheater(theater);
                seat.setRowName(rowName);
                seat.setColNumber(c);
                seats.add(seat);
            }
        }
        return seats;
    }

    private String toRowName(int rowIndex) {
        StringBuilder sb = new StringBuilder();
        while (rowIndex > 0) {
            rowIndex--;
            sb.insert(0, (char) ('A' + rowIndex % 26));
            rowIndex /= 26;
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeTheaterResponse> findAllTypeTheaters() {
        return typeTheaterRepository.findAll()
                .stream()
                .map(TypeTheaterResponse::from)
                .toList();
    }
}
