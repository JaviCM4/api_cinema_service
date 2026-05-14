package com.example.cinema.repositories.theater;

import com.example.cinema.models.theater.TypeTheater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TypeTheaterRepository extends JpaRepository<TypeTheater, UUID> {

    Optional<TypeTheater> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
