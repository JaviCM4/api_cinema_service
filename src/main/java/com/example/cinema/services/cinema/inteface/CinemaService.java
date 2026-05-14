package com.example.cinema.services.cinema.inteface;

import com.example.cinema.dtos.cinema.request.CreateCinemaRequest;
import com.example.cinema.dtos.cinema.request.UpdateCinemaRequest;
import com.example.cinema.dtos.cinema.response.CinemaResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface CinemaService {

    CinemaResponse createCinema(CreateCinemaRequest dto);

    CinemaResponse updateCinema(UUID cinemaId, UpdateCinemaRequest dto) throws ResourceNotFoundException;

    List<CinemaResponse> findAll();
}
