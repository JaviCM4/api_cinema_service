package com.example.cinema.dtos.cinema.response;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class RechargeResponse {

    BigDecimal newBalance;
    LocalDateTime transactionDate;
}
