package com.example.cinema.repositories.showtime;

import com.example.cinema.models.showtime.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, UUID> {

    List<Showtime> findByTheater_Id(UUID theaterId);

    List<Showtime> findByMovieId(UUID movieId);

    List<Showtime> findByTheater_IdAndDateShowtime(UUID theaterId, LocalDate date);

    List<Showtime> findByTheater_IdAndIsActiveTrueOrderByDateShowtimeAscStartShowtimeAsc(UUID theaterId);

    boolean existsByTheater_IdAndMovieIdAndDateShowtimeAndStartShowtime(
            UUID theaterId, UUID movieId, LocalDate date, java.time.LocalTime startTime);
}
