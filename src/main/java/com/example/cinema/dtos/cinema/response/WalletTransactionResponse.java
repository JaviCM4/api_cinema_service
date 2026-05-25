package com.example.cinema.dtos.cinema.response;

import com.example.cinema.models.cinema.WalletTransaction;
import com.example.cinema.models.enums.WalletTxType;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class WalletTransactionResponse {

    BigDecimal amount;
    WalletTxType type;
    String description;
    LocalDateTime transactionDate;

    public static WalletTransactionResponse from(WalletTransaction tx) {
        return new WalletTransactionResponse(
                tx.getAmount(),
                tx.getType(),
                tx.getDescription(),
                tx.getTransactionDate()
        );
    }
}
