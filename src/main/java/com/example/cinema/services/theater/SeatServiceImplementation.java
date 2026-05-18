package com.example.cinema.services.theater;

import com.example.cinema.dtos.theater.response.SeatResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.theater.Seat;
import com.example.cinema.repositories.theater.SeatRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import com.example.cinema.services.theater.inteface.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SeatServiceImplementation implements SeatService {

    private final SeatRepository seatRepository;
    private final TheaterRepository theaterRepository;

    @Autowired
    public SeatServiceImplementation(SeatRepository seatRepository, TheaterRepository theaterRepository) {
        this.seatRepository = seatRepository;
        this.theaterRepository = theaterRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> findByTheaterId(UUID theaterId) throws ResourceNotFoundException {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Sala no encontrada con id: " + theaterId);
        }
        return seatRepository.findByTheater_IdOrderByRowNameAscColNumberAsc(theaterId)
                .stream()
                .map(SeatResponse::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleSeatStatus(UUID seatId) throws ResourceNotFoundException {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Asiento no encontrado con id: " + seatId));
        seat.setActive(!seat.isActive());
        seatRepository.save(seat);
    }
}
