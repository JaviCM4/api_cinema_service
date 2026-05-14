package com.example.cinema.repositories.theater;

import com.example.cinema.models.theater.VersionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VersionTypeRepository extends JpaRepository<VersionType, UUID> {

    Optional<VersionType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
