package com.example.cinema.repositories.theater;

import com.example.cinema.models.theater.TheaterPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TheaterPricingRepository extends JpaRepository<TheaterPricing, UUID> {

    List<TheaterPricing> findByTheater_IdOrderByEffectiveDateDesc(UUID theaterId);

    Optional<TheaterPricing> findFirstByTheater_IdAndTypeTheater_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
            UUID theaterId, UUID typeTheaterId, LocalDate date);

    boolean existsByTheaterId(UUID theaterId);

    Optional<TheaterPricing> findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(UUID theaterId, LocalDate now);
}
