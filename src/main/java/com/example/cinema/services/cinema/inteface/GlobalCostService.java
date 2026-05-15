package com.example.cinema.services.cinema.inteface;

import com.example.cinema.dtos.cinema.request.CreateGlobalCostRequest;
import com.example.cinema.dtos.cinema.response.GlobalCostResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;

public interface GlobalCostService {

    void createGlobalCost(CreateGlobalCostRequest dto) throws ConflictException;

    GlobalCostResponse getLatest() throws ResourceNotFoundException;
}
