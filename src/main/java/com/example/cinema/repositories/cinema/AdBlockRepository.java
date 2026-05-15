package com.example.cinema.repositories.cinema;

import com.example.cinema.models.cinema.AdBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdBlockRepository extends JpaRepository<AdBlock, UUID> {

    List<AdBlock> findByCinema_IdOrderByStartDateDesc(UUID cinemaId);

    @Query("""
        SELECT a FROM AdBlock a
        WHERE a.cinema.id = :cinemaId
          AND a.startDate <= :date AND a.endDate >= :date
    """)
    List<AdBlock> findActiveByCinemaIdAndDate(@Param("cinemaId") UUID cinemaId, @Param("date") LocalDate date);

}
