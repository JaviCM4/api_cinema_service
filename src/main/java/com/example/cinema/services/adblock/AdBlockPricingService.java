package com.example.cinema.services.adblock;

import com.example.cinema.dtos.adblock.AdBlockPricingRequest;
import com.example.cinema.dtos.adblock.AdBlockPricingResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface AdBlockPricingService {

    AdBlockPricingResponse getAdBlockPricing(UUID cinemaId) throws ResourceNotFoundException;

    AdBlockPricingResponse createAdBlockPricing(UUID cinemaId, AdBlockPricingRequest request) throws ResourceNotFoundException, ConflictException;

    AdBlockPricingResponse updateAdBlockPricing(UUID cinemaId, AdBlockPricingRequest request) throws ResourceNotFoundException;

    List<AdBlockPricingResponse> getAllAdBlockPricings();
}
