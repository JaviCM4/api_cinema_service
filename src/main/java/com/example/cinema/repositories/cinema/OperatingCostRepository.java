package com.example.cinema.repositories.cinema;

import com.example.cinema.models.cinema.OperatingCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatingCostRepository extends JpaRepository<OperatingCost, UUID> {

    List<OperatingCost> findByCinema_IdOrderByEffectiveFromDesc(UUID cinemaId);

    List<OperatingCost> findByCinema_IdOrderByEffectiveFromAsc(UUID cinemaId);

    Optional<OperatingCost> findFirstByCinema_IdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            UUID cinemaId, LocalDate date);

    boolean existsByCinema_IdAndEffectiveFrom(UUID cinemaId, LocalDate effectiveFrom);
}
