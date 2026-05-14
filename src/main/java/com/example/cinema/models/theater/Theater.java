package com.example.cinema.models.theater;

import com.example.cinema.models.cinema.Cinema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "theater")
@Data
@NoArgsConstructor
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    @JsonIgnore
    private Cinema cinema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_theater_id", nullable = false)
    @JsonIgnore
    private TypeTheater typeTheater;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "rows", nullable = false)
    private Integer rows;

    @Column(name = "cols", nullable = false)
    private Integer cols;

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible = true;

    @Column(name = "allow_comments", nullable = false)
    private boolean allowComments = true;

    @Column(name = "allow_ratings", nullable = false)
    private boolean allowRatings = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
