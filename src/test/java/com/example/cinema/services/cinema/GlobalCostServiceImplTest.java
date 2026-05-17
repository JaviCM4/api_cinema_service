package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateGlobalCostRequest;
import com.example.cinema.dtos.cinema.response.GlobalCostResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.GlobalCost;
import com.example.cinema.repositories.cinema.GlobalCostRepository;
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
public class GlobalCostServiceImplTest {

    @Mock private GlobalCostRepository globalCostRepository;

    @InjectMocks
    private GlobalCostServiceImplementation globalCostService;

    // ─── createGlobalCost ────────────────────────────────────────────────────

    @Test
    void testCreateGlobalCost() throws Exception {
        LocalDate effectiveFrom = LocalDate.now().plusDays(1);
        CreateGlobalCostRequest request = new CreateGlobalCostRequest(
                new BigDecimal("300.00"), effectiveFrom);

        ArgumentCaptor<GlobalCost> captor = ArgumentCaptor.forClass(GlobalCost.class);
        when(globalCostRepository.existsByEffectiveFrom(effectiveFrom)).thenReturn(false);
        when(globalCostRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        globalCostService.createGlobalCost(request);

        verify(globalCostRepository).save(captor.capture());
        assertAll(
                () -> assertEquals(new BigDecimal("300.00"), captor.getValue().getDailyCost()),
                () -> assertEquals(effectiveFrom,            captor.getValue().getEffectiveFrom())
        );
    }

    @Test
    void testCreateGlobalCostDuplicateDate() {
        LocalDate effectiveFrom = LocalDate.now().plusDays(1);
        CreateGlobalCostRequest request = new CreateGlobalCostRequest(
                new BigDecimal("300.00"), effectiveFrom);

        when(globalCostRepository.existsByEffectiveFrom(effectiveFrom)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> globalCostService.createGlobalCost(request));
        verify(globalCostRepository, never()).save(any());
    }

    // ─── getLatest ───────────────────────────────────────────────────────────

    @Test
    void testGetLatest() throws Exception {
        GlobalCost globalCost = buildGlobalCost(new BigDecimal("450.00"), LocalDate.now());

        when(globalCostRepository.findFirstByOrderByEffectiveFromDesc())
                .thenReturn(Optional.of(globalCost));

        GlobalCostResponse result = globalCostService.getLatest();

        assertAll(
                () -> assertEquals(new BigDecimal("450.00"),      result.getDailyCost()),
                () -> assertEquals(globalCost.getEffectiveFrom(), result.getEffectiveFrom())
        );
    }

    @Test
    void testGetLatestReturnsNewestByEffectiveFrom() throws Exception {
        GlobalCost latest = buildGlobalCost(new BigDecimal("600.00"), LocalDate.now());

        when(globalCostRepository.findFirstByOrderByEffectiveFromDesc())
                .thenReturn(Optional.of(latest));

        GlobalCostResponse result = globalCostService.getLatest();

        assertEquals(new BigDecimal("600.00"), result.getDailyCost());
    }

    @Test
    void testGetLatestNoRecords() {
        when(globalCostRepository.findFirstByOrderByEffectiveFromDesc())
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> globalCostService.getLatest());
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private GlobalCost buildGlobalCost(BigDecimal dailyCost, LocalDate effectiveFrom) {
        GlobalCost gc = new GlobalCost();
        gc.setId(UUID.randomUUID());
        gc.setDailyCost(dailyCost);
        gc.setEffectiveFrom(effectiveFrom);
        gc.setCreatedAt(LocalDateTime.now());
        return gc;
    }
}
