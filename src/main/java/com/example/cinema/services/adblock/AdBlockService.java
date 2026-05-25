package com.example.cinema.services.adblock;

import com.example.cinema.dtos.adblock.AdBlockNowResponse;
import com.example.cinema.dtos.adblock.AdBlockRequest;
import com.example.cinema.dtos.adblock.AdBlockResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface AdBlockService {

    AdBlockResponse createAdBlock(UUID cinemaId, AdBlockRequest request) throws ResourceNotFoundException, ConflictException;

    List<AdBlockResponse> getAdBlocksByCinemaId(UUID cinemaId) throws ResourceNotFoundException;

    List<AdBlockResponse> getAllAdBlocks();

    AdBlockNowResponse getCurrentAdBlockStatus(UUID cinemaId) throws ResourceNotFoundException;
}
