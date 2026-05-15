package com.example.cinema.services.theater.inteface;

import com.example.cinema.dtos.theater.request.TheaterPrincingRequest;
import com.example.cinema.dtos.theater.response.TheaterPrincingResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;


public interface TheaterPricingService {

    TheaterPrincingResponse getTheaterPricing(UUID theaterId) throws ResourceNotFoundException, ConflictException;

    TheaterPrincingResponse createTheaterPricing(UUID theaterId, TheaterPrincingRequest dto) throws ResourceNotFoundException, ConflictException;

    TheaterPrincingResponse updateTheaterPricing(UUID theaterId, TheaterPrincingRequest dto) throws ResourceNotFoundException;

    BigDecimal getPriceForTheater(UUID theaterId) throws ResourceNotFoundException;

}
