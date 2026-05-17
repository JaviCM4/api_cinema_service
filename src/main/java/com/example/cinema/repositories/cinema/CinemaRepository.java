package com.example.cinema.repositories.cinema;

import com.example.cinema.models.cinema.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, UUID> {

    Optional<Cinema> findByAdminCinemaId(UUID adminCinemaId);

    List<Cinema> findByCountryId(UUID countryId);

    boolean existsByNameIgnoreCase(String name);
}
