package com.example.cinema.services.showtime.inteface;

import com.example.cinema.dtos.showtime.request.CreateShowtimeRequest;
import com.example.cinema.dtos.showtime.request.UpdateShowtimeRequest;
import com.example.cinema.dtos.showtime.response.ShowtimeByTheaterResponse;
import com.example.cinema.dtos.showtime.response.ShowtimeResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;

import com.example.cinema.models.theater.VersionType;

import java.util.List;
import java.util.UUID;

public interface ShowtimeService {

    void createShowtime(CreateShowtimeRequest dto) throws ResourceNotFoundException, ConflictException;

    void updateShowtime(UUID showtimeId, UpdateShowtimeRequest dto)
            throws ResourceNotFoundException, ConflictException;

    List<ShowtimeByTheaterResponse> findShowtimesByTheater(UUID theaterId);
}
