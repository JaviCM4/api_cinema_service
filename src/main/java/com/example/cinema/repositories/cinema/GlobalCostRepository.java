package com.example.cinema.repositories.cinema;

import com.example.cinema.models.cinema.GlobalCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GlobalCostRepository extends JpaRepository<GlobalCost, UUID> {

    boolean existsByEffectiveFrom(LocalDate effectiveFrom);

    Optional<GlobalCost> findFirstByOrderByEffectiveFromDesc();
}
