package com.example.cinema.models.theater;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "seat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"theater_id", "row_name", "col_number"}))
@Data
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    @JsonIgnore
    private Theater theater;

    @Column(name = "row_name", nullable = false, length = 5)
    private String rowName;

    @Column(name = "col_number", nullable = false)
    private Integer colNumber;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
