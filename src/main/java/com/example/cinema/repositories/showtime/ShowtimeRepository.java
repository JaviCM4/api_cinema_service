package com.example.cinema.repositories.showtime;

import com.example.cinema.models.showtime.Showtime;
import com.example.cinema.models.theater.VersionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, UUID> {

    @Query("SELECT s FROM Showtime s WHERE " +
           "(:movieId IS NULL OR s.movieId = :movieId) AND " +
           "(:theaterId IS NULL OR s.theater.id = :theaterId) AND " +
           "(:versionType IS NULL OR s.versionType = :versionType) " +
           "ORDER BY s.dateShowtime ASC, s.startShowtime ASC")
    List<Showtime> findByFilters(@Param("movieId") UUID movieId,
                                 @Param("theaterId") UUID theaterId,
                                 @Param("versionType") VersionType versionType);

    List<Showtime> findByTheater_Id(UUID theaterId);

    List<Showtime> findByMovieId(UUID movieId);

    List<Showtime> findByTheater_IdAndDateShowtime(UUID theaterId, LocalDate date);

    List<Showtime> findByTheater_IdAndIsActiveTrueOrderByDateShowtimeAscStartShowtimeAsc(UUID theaterId);

    List<Showtime> findByMovieIdAndIsActiveTrueOrderByDateShowtimeAscStartShowtimeAsc(UUID movieId);

    boolean existsByTheater_IdAndMovieIdAndDateShowtimeAndStartShowtime(
            UUID theaterId, UUID movieId, LocalDate date, LocalTime startTime);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Showtime s WHERE " +
           "s.theater.id = :theaterId AND s.dateShowtime = :date AND s.isActive = true AND " +
           "s.startShowtime < :endTime AND s.endShowtime > :startTime AND " +
           "(:excludeId IS NULL OR s.id <> :excludeId)")
    boolean existsOverlap(@Param("theaterId") UUID theaterId,
                          @Param("date") LocalDate date,
                          @Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime,
                          @Param("excludeId") UUID excludeId);
}
