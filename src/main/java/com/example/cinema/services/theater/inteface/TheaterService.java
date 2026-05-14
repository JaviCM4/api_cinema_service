package com.example.cinema.services.theater.inteface;

import com.example.cinema.dtos.theater.request.CreateTheaterRequest;
import com.example.cinema.dtos.theater.request.UpdateTheaterRequest;
import com.example.cinema.dtos.theater.response.TheaterResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.ResourceNotFoundException;

import java.util.UUID;

public interface TheaterService {

    void createTheater(CreateTheaterRequest dto) throws ResourceNotFoundException, ConflictException;

    void updateTheater(UUID theaterId, UpdateTheaterRequest dto) throws ResourceNotFoundException;

    TheaterResponse getTheater(UUID theaterId) throws ResourceNotFoundException;
}
