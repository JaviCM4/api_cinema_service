package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateOperatingCostRequest;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.kafka.CinemaEventProducer;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.cinema.OperatingCost;
import com.example.cinema.repositories.cinema.CinemaRepository;
import com.example.cinema.repositories.cinema.OperatingCostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OperatingCostServiceImplTest {

    private static final UUID CINEMA_ID = UUID.randomUUID();

    @Mock private OperatingCostRepository operatingCostRepository;
    @Mock private CinemaRepository  cinemaRepository;
    @Mock private CinemaEventProducer eventProducer;

    @InjectMocks
    private OperatingCostServiceImplementation operatingCostService;

    @Test
    void testCreateOperatingCost() throws Exception {
        // Arrange
        LocalDate effectiveFrom = LocalDate.now().plusDays(1);
        CreateOperatingCostRequest request = new CreateOperatingCostRequest(
                CINEMA_ID, new BigDecimal("150.00"), effectiveFrom);

        Cinema cinema = buildCinema();
        ArgumentCaptor<OperatingCost> captor = ArgumentCaptor.forClass(OperatingCost.class);

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(cinema));
        when(operatingCostRepository.existsByCinema_IdAndEffectiveFrom(CINEMA_ID, effectiveFrom))
                .thenReturn(false);
        when(operatingCostRepository.save(any(OperatingCost.class))).thenReturn(buildOperatingCost());

        // Act
        operatingCostService.createOperatingCost(request);

        // Assert
        assertAll(
                () -> verify(cinemaRepository).findById(CINEMA_ID),
                () -> verify(operatingCostRepository).existsByCinema_IdAndEffectiveFrom(CINEMA_ID, effectiveFrom),
                () -> verify(operatingCostRepository).save(captor.capture()),
                () -> assertEquals(cinema,                  captor.getValue().getCinema()),
                () -> assertEquals(new BigDecimal("150.00"), captor.getValue().getDailyCost()),
                () -> assertEquals(effectiveFrom,            captor.getValue().getEffectiveFrom()),
                () -> verify(eventProducer).publishOperatingCostCreated(any())
        );
    }

    @Test
    void testCreateOperatingCostCinemaNotFound() {
        // Arrange
        CreateOperatingCostRequest request = new CreateOperatingCostRequest(
                CINEMA_ID, new BigDecimal("150.00"), LocalDate.now().plusDays(1));

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> operatingCostService.createOperatingCost(request));
        verify(operatingCostRepository, never()).save(any());
    }

    @Test
    void testCreateOperatingCostDateConflict() {
        // Arrange
        LocalDate effectiveFrom = LocalDate.now().plusDays(1);
        CreateOperatingCostRequest request = new CreateOperatingCostRequest(
                CINEMA_ID, new BigDecimal("150.00"), effectiveFrom);

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(buildCinema()));
        when(operatingCostRepository.existsByCinema_IdAndEffectiveFrom(CINEMA_ID, effectiveFrom))
                .thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> operatingCostService.createOperatingCost(request));
        verify(operatingCostRepository, never()).save(any());
    }

    private Cinema buildCinema() {
        Cinema cinema = new Cinema();
        cinema.setId(CINEMA_ID);
        cinema.setName("Cinepolis Centro");
        cinema.setCreatedAt(LocalDateTime.now());
        cinema.setUpdatedAt(LocalDateTime.now());
        return cinema;
    }

    private OperatingCost buildOperatingCost() {
        OperatingCost cost = new OperatingCost();
        cost.setId(UUID.randomUUID());
        cost.setCinema(buildCinema());
        cost.setDailyCost(new BigDecimal("150.00"));
        cost.setEffectiveFrom(LocalDate.now().plusDays(1));
        cost.setCreatedAt(LocalDateTime.now());
        return cost;
    }
}
