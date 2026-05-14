package com.example.cinema.repositories.theater;

import com.example.cinema.models.theater.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByTheater_Id(UUID theaterId);

    List<Seat> findByTheater_IdAndIsActiveTrue(UUID theaterId);

    Optional<Seat> findByTheater_IdAndRowNameAndColNumber(UUID theaterId, String rowName, Integer colNumber);
}
