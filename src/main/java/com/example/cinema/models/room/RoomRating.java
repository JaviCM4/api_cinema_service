package com.example.cinema.models.room;

import com.example.cinema.models.theater.Theater;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "room_rating",
        uniqueConstraints = @UniqueConstraint(columnNames = {"theater_id", "user_id"}))
@Data
@NoArgsConstructor
public class RoomRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    @JsonIgnore
    private Theater theater;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "score", nullable = false)
    private Short score;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
