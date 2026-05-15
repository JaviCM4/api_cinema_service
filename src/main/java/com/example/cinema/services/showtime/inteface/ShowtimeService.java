package com.example.cinema.services.showtime.inteface;

import com.example.cinema.dtos.showtime.request.CreateShowtimeRequest;
import com.example.cinema.dtos.showtime.request.UpdateShowtimeRequest;
import com.example.cinema.dtos.showtime.response.ShowtimeResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ShowtimeService {

    void createShowtime(CreateShowtimeRequest dto) throws ResourceNotFoundException;

    void updateShowtime(UUID showtimeId, UpdateShowtimeRequest dto)
            throws ResourceNotFoundException, ConflictException;

    List<ShowtimeResponse> findShowtimes(UUID movieId, UUID theaterId, UUID versionTypeId);
}
