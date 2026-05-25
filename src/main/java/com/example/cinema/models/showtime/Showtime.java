package com.example.cinema.models.showtime;

import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.VersionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "showtime")
@Data
@NoArgsConstructor
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    @JsonIgnore
    private Theater theater;

    @Column(name = "movie_id", nullable = false)
    private UUID movieId;

    @Enumerated(EnumType.STRING)
    @Column(name = "version_type", nullable = false, length = 50)
    private VersionType versionType;

    @Column(name = "date_showtime", nullable = false)
    private LocalDate dateShowtime;

    @Column(name = "start_showtime", nullable = false)
    private LocalTime startShowtime;

    @Column(name = "end_showtime", nullable = false)
    private LocalTime endShowtime;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
