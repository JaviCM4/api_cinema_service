package com.example.cinema.repositories.room;

import com.example.cinema.models.room.RoomComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomCommentRepository extends JpaRepository<RoomComment, UUID> {

    List<RoomComment> findByTheater_IdOrderByCreatedAtDesc(UUID theaterId);

    List<RoomComment> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
