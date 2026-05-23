package com.example.cinema.dtos.cinema.response;

import com.example.cinema.models.cinema.Company;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class CompanyResponse {

    UUID id;
    String name;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
