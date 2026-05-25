package com.example.cinema.repositories.cinema;

import com.example.cinema.models.cinema.CinemaWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CinemaWalletRepository extends JpaRepository<CinemaWallet, UUID> {

    Optional<CinemaWallet> findByCinema_Id(UUID cinemaId);

    @Query("""
    SELECT w.balance
    FROM CinemaWallet w
    WHERE w.cinema.id = :cinemaId
""")
    BigDecimal getBalanceByCinemaId(@Param("cinemaId") UUID cinemaId);
}
