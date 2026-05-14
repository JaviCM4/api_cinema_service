package com.example.cinema.repositories.theater;

import com.example.cinema.models.theater.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, UUID> {

    List<Theater> findByCinema_Id(UUID cinemaId);

    List<Theater> findByCinema_IdAndIsVisibleTrue(UUID cinemaId);

    boolean existsByCinema_IdAndNameIgnoreCase(UUID cinemaId, String name);
}
