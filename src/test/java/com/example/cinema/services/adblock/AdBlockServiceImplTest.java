package com.example.cinema.services.adblock;

import com.example.cinema.dtos.adblock.AdBlockNowResponse;
import com.example.cinema.dtos.adblock.AdBlockRequest;
import com.example.cinema.dtos.adblock.AdBlockResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.kafka.CinemaEventProducer;
import com.example.cinema.models.cinema.*;
import com.example.cinema.repositories.cinema.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdBlockServiceImplTest {
    private static final UUID CINEMA_ID = UUID.randomUUID();
    private static final BigDecimal PRICE_PER_DAY = new BigDecimal("10.00");
    private static final int DAYS_BLOCKED = 3;
    private static final BigDecimal AMOUNT_TO_PAY = PRICE_PER_DAY.multiply(BigDecimal.valueOf(DAYS_BLOCKED));

    @Mock private AdBlockRepository adBlockRepository;
    @Mock private AdBlockPricingRepository adBlockPricingRepository;
    @Mock private CinemaRepository cinemaRepository;
    @Mock private CinemaWalletRepository walletRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private CinemaEventProducer eventProducer;

    @InjectMocks
    private AdBlockServiceImpl service;

    private Cinema cinema() {
        Cinema c = new Cinema();
        c.setId(CINEMA_ID);
        c.setName("Cine Test");
        return c;
    }

    private AdBlockPricing pricing() {
        AdBlockPricing p = new AdBlockPricing();
        p.setPricePerDay(PRICE_PER_DAY);
        return p;
    }

    private CinemaWallet wallet(BigDecimal balance) {
        CinemaWallet w = new CinemaWallet();
        w.setBalance(balance);
        return w;
    }

    private AdBlockRequest request() {
        return new AdBlockRequest(DAYS_BLOCKED);
    }

    private AdBlock adBlock(LocalDate start, LocalDate end) {
        AdBlock ab = new AdBlock();
        ab.setCinema(cinema());
        ab.setDaysBlocked(DAYS_BLOCKED);
        ab.setStartDate(start);
        ab.setEndDate(end);
        ab.setAmountPaid(AMOUNT_TO_PAY);
        return ab;
    }

    // ------------------- createAdBlock -------------------
    @Test
    void createAdBlock_Success() throws Exception {
        LocalDate today = LocalDate.now();
        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(cinema()));
        when(adBlockRepository.findByCinema_IdOrderByStartDateDesc(CINEMA_ID)).thenReturn(Collections.emptyList());
        when(adBlockPricingRepository.findByCinemaId(CINEMA_ID)).thenReturn(Optional.of(pricing()));
        when(walletRepository.getBalanceByCinemaId(CINEMA_ID)).thenReturn(AMOUNT_TO_PAY);
        when(walletRepository.findByCinema_Id(CINEMA_ID)).thenReturn(Optional.of(wallet(AMOUNT_TO_PAY)));
        when(adBlockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(walletTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdBlockResponse response = service.createAdBlock(CINEMA_ID, request());
        assertNotNull(response);
        assertEquals(DAYS_BLOCKED, response.getDaysBlocked());
        assertEquals(AMOUNT_TO_PAY, response.getAmountPaid());
        verify(eventProducer).publishAdBlockCreated(any());
    }

    @Test
    void createAdBlock_CinemaNotFound() {
        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createAdBlock(CINEMA_ID, request()));
    }

    @Test
    void createAdBlock_PricingNotFound() {
        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(cinema()));
        when(adBlockRepository.findByCinema_IdOrderByStartDateDesc(CINEMA_ID)).thenReturn(Collections.emptyList());
        when(adBlockPricingRepository.findByCinemaId(CINEMA_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createAdBlock(CINEMA_ID, request()));
    }

    @Test
    void createAdBlock_InsufficientFunds() {
        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(cinema()));
        when(adBlockRepository.findByCinema_IdOrderByStartDateDesc(CINEMA_ID)).thenReturn(Collections.emptyList());
        when(adBlockPricingRepository.findByCinemaId(CINEMA_ID)).thenReturn(Optional.of(pricing()));
        when(walletRepository.getBalanceByCinemaId(CINEMA_ID)).thenReturn(BigDecimal.ZERO);
        assertThrows(ConflictException.class, () -> service.createAdBlock(CINEMA_ID, request()));
    }

    @Test
    void createAdBlock_WalletNotFound() {
        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(cinema()));
        when(adBlockRepository.findByCinema_IdOrderByStartDateDesc(CINEMA_ID)).thenReturn(Collections.emptyList());
        when(adBlockPricingRepository.findByCinemaId(CINEMA_ID)).thenReturn(Optional.of(pricing()));
        when(walletRepository.getBalanceByCinemaId(CINEMA_ID)).thenReturn(AMOUNT_TO_PAY);
        when(walletRepository.findByCinema_Id(CINEMA_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createAdBlock(CINEMA_ID, request()));
    }

    @Test
    void createAdBlock_WithExistingBlock_StartsAfterPreviousEnd() throws Exception {
        LocalDate previousEnd = LocalDate.now().plusDays(2);
        AdBlock previousBlock = adBlock(previousEnd.minusDays(DAYS_BLOCKED - 1), previousEnd);

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(cinema()));
        when(adBlockRepository.findByCinema_IdOrderByStartDateDesc(CINEMA_ID))
                .thenReturn(List.of(previousBlock));
        when(adBlockPricingRepository.findByCinemaId(CINEMA_ID)).thenReturn(Optional.of(pricing()));
        when(walletRepository.getBalanceByCinemaId(CINEMA_ID)).thenReturn(AMOUNT_TO_PAY);
        when(walletRepository.findByCinema_Id(CINEMA_ID)).thenReturn(Optional.of(wallet(AMOUNT_TO_PAY)));
        when(adBlockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(walletTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdBlockResponse response = service.createAdBlock(CINEMA_ID, request());

        assertNotNull(response);
        assertEquals(previousEnd.plusDays(1).toString(), response.getStartDate());
        assertEquals(previousEnd.plusDays(DAYS_BLOCKED).toString(), response.getEndDate());
    }

    // ------------------- getAdBlocksByCinemaId -------------------
    @Test
    void getAdBlocksByCinemaId_ReturnsList() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(DAYS_BLOCKED - 1);
        when(adBlockRepository.findByCinema_IdOrderByStartDateDesc(CINEMA_ID))
                .thenReturn(List.of(adBlock(start, end)));
        List<AdBlockResponse> result = service.getAdBlocksByCinemaId(CINEMA_ID);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAdBlocksByCinemaId_EmptyList() throws Exception {
        when(adBlockRepository.findByCinema_IdOrderByStartDateDesc(CINEMA_ID)).thenReturn(Collections.emptyList());
        List<AdBlockResponse> result = service.getAdBlocksByCinemaId(CINEMA_ID);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ------------------- getAllAdBlocks -------------------
    @Test
    void getAllAdBlocks_ReturnsList() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(DAYS_BLOCKED - 1);
        when(adBlockRepository.findAll()).thenReturn(List.of(adBlock(start, end)));
        List<AdBlockResponse> result = service.getAllAdBlocks();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllAdBlocks_EmptyList() {
        when(adBlockRepository.findAll()).thenReturn(Collections.emptyList());
        List<AdBlockResponse> result = service.getAllAdBlocks();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ------------------- getCurrentAdBlockStatus -------------------
    @Test
    void getCurrentAdBlockStatus_NotBlocked() throws Exception {
        when(adBlockRepository.findActiveByCinemaIdAndDate(CINEMA_ID, LocalDate.now()))
                .thenReturn(Collections.emptyList());

        AdBlockNowResponse response = service.getCurrentAdBlockStatus(CINEMA_ID);

        assertFalse(response.isBlocked());
        assertNull(response.getBlockEndDate());
    }


}
