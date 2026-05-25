package com.example.cinema.services.cinema.inteface;

import com.example.cinema.dtos.cinema.request.CreateOperatingCostRequest;
import com.example.cinema.dtos.cinema.response.CinemaOperatingCostSummaryResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;

import java.util.List;

public interface OperatingCostService {

    void createOperatingCost(CreateOperatingCostRequest dto)
            throws ResourceNotFoundException, ConflictException;

    List<CinemaOperatingCostSummaryResponse> getAllOperatingCostSummaries();
}
