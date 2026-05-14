package com.example.cinema.repositories.room;

import com.example.cinema.models.room.RoomRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRatingRepository extends JpaRepository<RoomRating, UUID> {

    List<RoomRating> findByTheater_Id(UUID theaterId);

    Optional<RoomRating> findByTheater_IdAndUserId(UUID theaterId, UUID userId);

    @Query("SELECT AVG(r.score) FROM RoomRating r WHERE r.theater.id = :theaterId")
    Double findAverageScoreByTheater_Id(UUID theaterId);
}
