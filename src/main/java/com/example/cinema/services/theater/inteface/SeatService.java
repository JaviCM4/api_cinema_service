package com.example.cinema.services.theater.inteface;

import com.example.cinema.dtos.theater.response.SeatResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface SeatService {

    List<SeatResponse> findByTheaterId(UUID theaterId) throws ResourceNotFoundException;

    void toggleSeatStatus(UUID seatId) throws ResourceNotFoundException;
}
