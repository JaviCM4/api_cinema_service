package com.example.cinema.repositories.cinema;

import com.example.cinema.models.cinema.WalletTransaction;
import com.example.cinema.models.enums.WalletTxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    List<WalletTransaction> findByCinemaWallet_IdOrderByTransactionDateDesc(UUID cinemaWalletId);

    List<WalletTransaction> findByCinemaWallet_IdAndType(UUID cinemaWalletId, WalletTxType type);

    @Query("SELECT SUM(t.amount) FROM WalletTransaction t WHERE t.cinemaWallet.id = :walletId AND t.type = :type")
    java.math.BigDecimal sumAmountByWalletIdAndType(@Param("walletId") UUID walletId, @Param("type") WalletTxType type);
}
