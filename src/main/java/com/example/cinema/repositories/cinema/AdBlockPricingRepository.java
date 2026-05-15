package com.example.cinema.repositories.cinema;

import com.example.cinema.models.cinema.AdBlockPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdBlockPricingRepository extends JpaRepository<AdBlockPricing, UUID> {
    Optional <AdBlockPricing> findByCinemaId(UUID cinemaId);
}
