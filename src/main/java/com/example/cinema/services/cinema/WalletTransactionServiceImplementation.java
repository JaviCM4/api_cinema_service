package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateWalletTransactionRequest;
import com.example.cinema.dtos.cinema.response.WalletTransactionResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.CinemaWallet;
import com.example.cinema.models.cinema.WalletTransaction;
import com.example.cinema.models.enums.WalletTxType;
import com.example.cinema.repositories.cinema.CinemaWalletRepository;
import com.example.cinema.repositories.cinema.WalletTransactionRepository;
import com.example.cinema.services.cinema.inteface.WalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WalletTransactionServiceImplementation implements WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final CinemaWalletRepository cinemaWalletRepository;

    @Autowired
    public WalletTransactionServiceImplementation(WalletTransactionRepository walletTransactionRepository, CinemaWalletRepository cinemaWalletRepository) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.cinemaWalletRepository = cinemaWalletRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRecharge(UUID cinemaId, CreateWalletTransactionRequest dto)
            throws ResourceNotFoundException {
        CinemaWallet wallet = cinemaWalletRepository.findByCinema_Id(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartera Digital no encontrada para el cine con id: " + cinemaId));

        walletTransactionRepository.save(dto.createEntity(wallet));
        wallet.setBalance(wallet.getBalance().add(dto.getAmount()));
        cinemaWalletRepository.save(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> findAll(UUID cinemaId)
            throws ResourceNotFoundException {
        CinemaWallet wallet = cinemaWalletRepository.findByCinema_Id(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartera Digital no encontrada para el cine con id: " + cinemaId));

        return walletTransactionRepository
                .findByCinemaWallet_IdOrderByTransactionDateDesc(wallet.getId())
                .stream()
                .map(WalletTransactionResponse::from)
                .toList();
    }
}
