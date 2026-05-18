package com.example.cinema.controllers;

import com.example.cinema.dtos.cinema.request.CreateWalletTransactionRequest;
import com.example.cinema.dtos.cinema.response.RechargeResponse;
import com.example.cinema.dtos.cinema.response.WalletTransactionResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.cinema.inteface.WalletTransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/wallet-transactions")
public class WalletTransactionController {

    private final WalletTransactionService walletTransactionService;

    @Autowired
    public WalletTransactionController(WalletTransactionService walletTransactionService) {
        this.walletTransactionService = walletTransactionService;
    }

    @PostMapping
    public ResponseEntity<RechargeResponse> createRecharge(@Valid @RequestBody CreateWalletTransactionRequest dto)
            throws ResourceNotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletTransactionService.createRecharge(dto));
    }

    @GetMapping("/{adminCinemaId}")
    public ResponseEntity<List<WalletTransactionResponse>> findAll(@PathVariable UUID adminCinemaId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(walletTransactionService.findAll(adminCinemaId));
    }
}
