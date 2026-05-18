package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateWalletTransactionRequest;
import com.example.cinema.dtos.cinema.response.RechargeResponse;
import com.example.cinema.dtos.cinema.response.WalletTransactionResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.cinema.CinemaWallet;
import com.example.cinema.models.cinema.WalletTransaction;
import com.example.cinema.models.enums.WalletTxType;
import com.example.cinema.repositories.cinema.CinemaRepository;
import com.example.cinema.repositories.cinema.CinemaWalletRepository;
import com.example.cinema.repositories.cinema.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletTransactionServiceImplTest {

    private static final UUID CINEMA_ID  = UUID.randomUUID();
    private static final UUID ADMIN_ID   = UUID.randomUUID();
    private static final UUID WALLET_ID  = UUID.randomUUID();

    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private CinemaWalletRepository      cinemaWalletRepository;
    @Mock private CinemaRepository            cinemaRepository;

    @InjectMocks
    private WalletTransactionServiceImplementation walletTransactionService;

    // ─── createRecharge ──────────────────────────────────────────────────────

    @Test
    void testCreateRecharge() throws Exception {
        CreateWalletTransactionRequest request =
                new CreateWalletTransactionRequest(ADMIN_ID, new BigDecimal("100.00"), "Recarga de prueba");

        CinemaWallet wallet = buildWallet(new BigDecimal("50.00"));
        LocalDateTime now = LocalDateTime.now();
        ArgumentCaptor<WalletTransaction> txCaptor    = ArgumentCaptor.forClass(WalletTransaction.class);
        ArgumentCaptor<CinemaWallet>      walletCaptor = ArgumentCaptor.forClass(CinemaWallet.class);

        when(cinemaRepository.findByAdminCinemaId(ADMIN_ID)).thenReturn(Optional.of(buildCinema()));
        when(cinemaWalletRepository.findByCinema_Id(CINEMA_ID)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.save(any())).thenAnswer(inv -> {
            WalletTransaction tx = inv.getArgument(0);
            tx.setTransactionDate(now);
            return tx;
        });
        when(cinemaWalletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RechargeResponse result = walletTransactionService.createRecharge(request);

        verify(walletTransactionRepository).save(txCaptor.capture());
        verify(cinemaWalletRepository).save(walletCaptor.capture());

        WalletTransaction savedTx = txCaptor.getValue();
        assertAll(
                () -> assertEquals(wallet,               savedTx.getCinemaWallet()),
                () -> assertEquals(new BigDecimal("100.00"), savedTx.getAmount()),
                () -> assertEquals(WalletTxType.RECHARGE, savedTx.getType()),
                () -> assertEquals("Recarga de prueba",  savedTx.getDescription())
        );

        CinemaWallet updatedWallet = walletCaptor.getValue();
        assertEquals(new BigDecimal("150.00"), updatedWallet.getBalance());

        assertAll(
                () -> assertEquals(new BigDecimal("150.00"), result.getNewBalance()),
                () -> assertEquals(now, result.getTransactionDate())
        );
    }

    @Test
    void testCreateRechargeWithoutDescription() throws Exception {
        CreateWalletTransactionRequest request =
                new CreateWalletTransactionRequest(ADMIN_ID, new BigDecimal("200.00"), null);

        CinemaWallet wallet = buildWallet(BigDecimal.ZERO);

        when(cinemaRepository.findByAdminCinemaId(ADMIN_ID)).thenReturn(Optional.of(buildCinema()));
        when(cinemaWalletRepository.findByCinema_Id(CINEMA_ID)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cinemaWalletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RechargeResponse result = walletTransactionService.createRecharge(request);

        ArgumentCaptor<WalletTransaction> txCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(txCaptor.capture());
        assertAll(
                () -> assertNull(txCaptor.getValue().getDescription()),
                () -> assertEquals(new BigDecimal("200.00"), result.getNewBalance())
        );
    }

    @Test
    void testCreateRechargeAdminNotFound() {
        CreateWalletTransactionRequest request =
                new CreateWalletTransactionRequest(ADMIN_ID, new BigDecimal("50.00"), null);

        when(cinemaRepository.findByAdminCinemaId(ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletTransactionService.createRecharge(request));

        verify(walletTransactionRepository, never()).save(any());
        verify(cinemaWalletRepository, never()).save(any());
    }

    @Test
    void testCreateRechargeWalletNotFound() {
        CreateWalletTransactionRequest request =
                new CreateWalletTransactionRequest(ADMIN_ID, new BigDecimal("50.00"), null);

        when(cinemaRepository.findByAdminCinemaId(ADMIN_ID)).thenReturn(Optional.of(buildCinema()));
        when(cinemaWalletRepository.findByCinema_Id(CINEMA_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletTransactionService.createRecharge(request));

        verify(walletTransactionRepository, never()).save(any());
        verify(cinemaWalletRepository, never()).save(any());
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void testFindAll() throws Exception {
        CinemaWallet wallet = buildWallet(new BigDecimal("300.00"));

        WalletTransaction tx1 = buildTransaction(wallet, new BigDecimal("200.00"), "Primera recarga",
                LocalDateTime.now().minusDays(1));
        WalletTransaction tx2 = buildTransaction(wallet, new BigDecimal("100.00"), "Segunda recarga",
                LocalDateTime.now());

        when(cinemaRepository.findByAdminCinemaId(ADMIN_ID)).thenReturn(Optional.of(buildCinema()));
        when(cinemaWalletRepository.findByCinema_Id(CINEMA_ID)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findByCinemaWallet_IdOrderByTransactionDateDesc(WALLET_ID))
                .thenReturn(List.of(tx2, tx1));

        List<WalletTransactionResponse> result = walletTransactionService.findAll(ADMIN_ID);

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(new BigDecimal("100.00"), result.get(0).getAmount()),
                () -> assertEquals(new BigDecimal("200.00"), result.get(1).getAmount()),
                () -> assertEquals(WalletTxType.RECHARGE, result.get(0).getType())
        );
    }

    @Test
    void testFindAllEmpty() throws Exception {
        CinemaWallet wallet = buildWallet(BigDecimal.ZERO);

        when(cinemaRepository.findByAdminCinemaId(ADMIN_ID)).thenReturn(Optional.of(buildCinema()));
        when(cinemaWalletRepository.findByCinema_Id(CINEMA_ID)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findByCinemaWallet_IdOrderByTransactionDateDesc(WALLET_ID))
                .thenReturn(List.of());

        List<WalletTransactionResponse> result = walletTransactionService.findAll(ADMIN_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllWalletNotFound() {
        when(cinemaRepository.findByAdminCinemaId(ADMIN_ID)).thenReturn(Optional.of(buildCinema()));
        when(cinemaWalletRepository.findByCinema_Id(CINEMA_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletTransactionService.findAll(ADMIN_ID));

        verify(walletTransactionRepository, never()).findByCinemaWallet_IdOrderByTransactionDateDesc(any());
    }

    @Test
    void testFindAllAdminNotFound() {
        when(cinemaRepository.findByAdminCinemaId(ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletTransactionService.findAll(ADMIN_ID));

        verify(cinemaWalletRepository, never()).findByCinema_Id(any());
        verify(walletTransactionRepository, never()).findByCinemaWallet_IdOrderByTransactionDateDesc(any());
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private CinemaWallet buildWallet(BigDecimal balance) {
        CinemaWallet wallet = new CinemaWallet();
        wallet.setId(WALLET_ID);
        Cinema cinema = new Cinema();
        cinema.setId(CINEMA_ID);
        wallet.setCinema(cinema);
        wallet.setBalance(balance);
        return wallet;
    }

    private Cinema buildCinema() {
        Cinema cinema = new Cinema();
        cinema.setId(CINEMA_ID);
        cinema.setAdminCinemaId(ADMIN_ID);
        return cinema;
    }

    private WalletTransaction buildTransaction(CinemaWallet wallet, BigDecimal amount,
                                               String description, LocalDateTime date) {
        WalletTransaction tx = new WalletTransaction();
        tx.setId(UUID.randomUUID());
        tx.setCinemaWallet(wallet);
        tx.setAmount(amount);
        tx.setType(WalletTxType.RECHARGE);
        tx.setDescription(description);
        tx.setTransactionDate(date);
        return tx;
    }
}
