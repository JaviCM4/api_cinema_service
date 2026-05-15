package com.example.cinema.services.cinema.inteface;

import com.example.cinema.dtos.cinema.request.CreateWalletTransactionRequest;
import com.example.cinema.dtos.cinema.response.WalletTransactionResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface WalletTransactionService {

    void createRecharge(UUID cinemaId, CreateWalletTransactionRequest dto)
            throws ResourceNotFoundException;

    List<WalletTransactionResponse> findAll(UUID cinemaId)
            throws ResourceNotFoundException;
}
