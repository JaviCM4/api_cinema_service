package com.example.cinema.repositories.cinema;

import com.example.cinema.models.cinema.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Company> findByNameIgnoreCase(String name);
}
