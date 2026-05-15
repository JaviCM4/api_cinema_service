package com.example.cinema.services.theater;

import com.example.cinema.dtos.theater.request.CreateTheaterRequest;
import com.example.cinema.dtos.theater.request.UpdateTheaterRequest;
import com.example.cinema.dtos.theater.response.SeatResponse;
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
import com.example.cinema.services.theater.inteface.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TheaterServiceImplementation implements TheaterService {

    private final TheaterRepository theaterRepository;
    private final SeatRepository seatRepository;
    private final CinemaRepository cinemaRepository;
    private final TypeTheaterRepository typeTheaterRepository;

    @Autowired
    public TheaterServiceImplementation(TheaterRepository theaterRepository, SeatRepository seatRepository, CinemaRepository cinemaRepository, TypeTheaterRepository typeTheaterRepository) {
        this.theaterRepository = theaterRepository;
        this.seatRepository = seatRepository;
        this.cinemaRepository = cinemaRepository;
        this.typeTheaterRepository = typeTheaterRepository;
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

        if (dto.getTypeTheaterId() != null) {
            TypeTheater typeTheater = typeTheaterRepository.findById(dto.getTypeTheaterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de sala no encontrado con id: " + dto.getTypeTheaterId()));
            theater.setTypeTheater(typeTheater);
        }
        if (dto.getName() != null) {
            theater.setName(dto.getName());
        }
        if (dto.getIsVisible() != null) {
            theater.setVisible(dto.getIsVisible());
        }
        if (dto.getAllowComments() != null) {
            theater.setAllowComments(dto.getAllowComments());
        }
        if (dto.getAllowRatings() != null) {
            theater.setAllowRatings(dto.getAllowRatings());
        }

        theaterRepository.save(theater);
    }

    @Override
    @Transactional(readOnly = true)
    public TheaterResponse getTheater(UUID theaterId) throws ResourceNotFoundException {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id: " + theaterId));

        List<SeatResponse> seatResponses = seatRepository.findByTheater_Id(theaterId)
                .stream().map(SeatResponse::from).toList();
        return TheaterResponse.from(theater, seatResponses);
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
}
