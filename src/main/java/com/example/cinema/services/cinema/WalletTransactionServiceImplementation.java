package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateWalletTransactionRequest;
import com.example.cinema.dtos.cinema.response.WalletTransactionResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.cinema.CinemaWallet;
import com.example.cinema.models.cinema.WalletTransaction;
import com.example.cinema.repositories.cinema.CinemaRepository;
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
    private final CinemaRepository cinemaRepository;

    @Autowired
    public WalletTransactionServiceImplementation(WalletTransactionRepository walletTransactionRepository, CinemaWalletRepository cinemaWalletRepository, CinemaRepository cinemaRepository) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.cinemaWalletRepository = cinemaWalletRepository;
        this.cinemaRepository = cinemaRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRecharge(CreateWalletTransactionRequest dto)
            throws ResourceNotFoundException {
        Cinema cinema = cinemaRepository.findByAdminCinemaId(dto.getAdminCinemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found"));

        CinemaWallet wallet = cinemaWalletRepository.findByCinema_Id(cinema.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cartera Digital no encontrada para el cine con id: " + cinema.getName()));

        walletTransactionRepository.save(dto.createEntity(wallet));
        wallet.setBalance(wallet.getBalance().add(dto.getAmount()));
        cinemaWalletRepository.save(wallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WalletTransactionResponse> findAll(UUID adminCinemaId) throws ResourceNotFoundException {
        Cinema cinema = cinemaRepository.findByAdminCinemaId(adminCinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found"));

        CinemaWallet wallet = cinemaWalletRepository.findByCinema_Id(cinema.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cartera Digital no encontrada para el cine con id: " + cinema.getId()));

        return walletTransactionRepository
                .findByCinemaWallet_IdOrderByTransactionDateDesc(wallet.getId())
                .stream()
                .map(WalletTransactionResponse::from)
                .toList();
    }
}
