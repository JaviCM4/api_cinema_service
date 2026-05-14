package com.example.cinema.repositories.cinema;

import com.example.cinema.models.cinema.CinemaWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CinemaWalletRepository extends JpaRepository<CinemaWallet, UUID> {

    Optional<CinemaWallet> findByCinema_Id(UUID cinemaId);
}
