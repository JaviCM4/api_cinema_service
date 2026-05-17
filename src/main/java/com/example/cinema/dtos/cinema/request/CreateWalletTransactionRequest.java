package com.example.cinema.dtos.cinema.request;

import com.example.cinema.models.cinema.CinemaWallet;
import com.example.cinema.models.cinema.WalletTransaction;
import com.example.cinema.models.enums.WalletTxType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
public class CreateWalletTransactionRequest {

    @NotNull(message = "El id del usuario es requerido")
    UUID adminCinemaId;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    BigDecimal amount;

    @Size(max = 255, message = "La descripción no debe superar 255 caracteres")
    String description;

    public WalletTransaction createEntity(CinemaWallet wallet) {
        WalletTransaction walletTransaction = new WalletTransaction();
        walletTransaction.setCinemaWallet(wallet);
        walletTransaction.setAmount(amount);
        walletTransaction.setType(WalletTxType.RECHARGE);
        walletTransaction.setDescription(description);
        return walletTransaction;
    }
}
