package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateCinemaRequest;
import com.example.cinema.dtos.cinema.request.UpdateCinemaRequest;
import com.example.cinema.dtos.cinema.response.CinemaResponse;
import com.example.cinema.dtos.cinema.response.CinemaSummaryResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.cinema.CinemaWallet;
import com.example.cinema.models.cinema.GlobalCost;
import com.example.cinema.models.cinema.OperatingCost;
import com.example.cinema.repositories.cinema.CinemaRepository;
import com.example.cinema.repositories.cinema.CinemaWalletRepository;
import com.example.cinema.repositories.cinema.GlobalCostRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CinemaServiceImplTest {

    private static final UUID CINEMA_ID  = UUID.randomUUID();
    private static final UUID ADMIN_ID   = UUID.randomUUID();
    private static final UUID COUNTRY_ID = UUID.randomUUID();

    @Mock private CinemaRepository cinemaRepository;
    @Mock private CinemaWalletRepository cinemaWalletRepository;
    @Mock private OperatingCostRepository operatingCostRepository;
    @Mock private GlobalCostRepository globalCostRepository;

    @InjectMocks
    private CinemaServiceImplementation cinemaService;

    @Test
    void testCreateCinema() throws Exception {
        // Arrange
        LocalDate effectiveFrom = LocalDate.now();
        CreateCinemaRequest request = new CreateCinemaRequest(
                ADMIN_ID, COUNTRY_ID, "Cinepolis Centro",
                "Calle 1 #10", "+573001234567", "cinema@mail.com", effectiveFrom);

        Cinema savedCinema = buildCinema("Cinepolis Centro");

        ArgumentCaptor<CinemaWallet>  walletCaptor = ArgumentCaptor.forClass(CinemaWallet.class);
        ArgumentCaptor<OperatingCost> costCaptor   = ArgumentCaptor.forClass(OperatingCost.class);

        when(cinemaRepository.save(any(Cinema.class))).thenReturn(savedCinema);
        when(globalCostRepository.findFirstByOrderByEffectiveFromDesc())
                .thenReturn(Optional.of(buildGlobalCost(new BigDecimal("500.00"))));
        when(cinemaWalletRepository.save(any(CinemaWallet.class))).thenReturn(new CinemaWallet());
        when(operatingCostRepository.save(any(OperatingCost.class))).thenReturn(new OperatingCost());

        // Act
        cinemaService.createCinema(request);

        // Assert
        assertAll(
                () -> verify(cinemaRepository).save(any(Cinema.class)),
                () -> verify(cinemaWalletRepository).save(walletCaptor.capture()),
                () -> verify(operatingCostRepository).save(costCaptor.capture()),
                () -> assertEquals(BigDecimal.ZERO,            walletCaptor.getValue().getBalance()),
                () -> assertEquals(savedCinema,                walletCaptor.getValue().getCinema()),
                () -> assertEquals(new BigDecimal("500.00"),   costCaptor.getValue().getDailyCost()),
                () -> assertEquals(effectiveFrom,              costCaptor.getValue().getEffectiveFrom()),
                () -> assertEquals(savedCinema,                costCaptor.getValue().getCinema())
        );
    }

    @Test
    void testCreateCinemaWithOptionalFieldsNull() throws Exception {
        // Arrange
        LocalDate effectiveFrom = LocalDate.now().minusDays(10);
        CreateCinemaRequest request = new CreateCinemaRequest(
                ADMIN_ID, COUNTRY_ID, "Cinepolis Sur", null, null, null, effectiveFrom);

        Cinema savedCinema = buildCinema("Cinepolis Sur");

        when(cinemaRepository.save(any(Cinema.class))).thenReturn(savedCinema);
        when(globalCostRepository.findFirstByOrderByEffectiveFromDesc())
                .thenReturn(Optional.of(buildGlobalCost(new BigDecimal("350.00"))));
        when(cinemaWalletRepository.save(any(CinemaWallet.class))).thenReturn(new CinemaWallet());
        when(operatingCostRepository.save(any(OperatingCost.class))).thenReturn(new OperatingCost());

        // Act
        cinemaService.createCinema(request);

        // Assert
        assertAll(
                () -> verify(cinemaRepository).save(any(Cinema.class)),
                () -> verify(cinemaWalletRepository).save(any(CinemaWallet.class)),
                () -> verify(operatingCostRepository).save(any(OperatingCost.class))
        );
    }

    @Test
    void testCreateCinemaNoGlobalCost() {
        // Arrange
        LocalDate effectiveFrom = LocalDate.now();
        CreateCinemaRequest request = new CreateCinemaRequest(
                ADMIN_ID, COUNTRY_ID, "Cinepolis Sur", null, null, null, effectiveFrom);

        when(globalCostRepository.findFirstByOrderByEffectiveFromDesc()).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> cinemaService.createCinema(request));
        verify(cinemaRepository, never()).save(any());
    }

    @Test
    void testUpdateCinema() throws Exception {
        // Arrange
        UpdateCinemaRequest request = new UpdateCinemaRequest(
                "Cinepolis Norte", "Av. Principal 5", "+573009876543", "norte@mail.com");

        ArgumentCaptor<Cinema> captor = ArgumentCaptor.forClass(Cinema.class);
        Cinema existing = buildCinema("Cinepolis Centro");

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(existing));

        // Act
        cinemaService.updateCinema(CINEMA_ID, request);

        // Assert
        assertAll(
                () -> verify(cinemaRepository).save(captor.capture()),
                () -> assertEquals("Cinepolis Norte",  captor.getValue().getName()),
                () -> assertEquals("Av. Principal 5",  captor.getValue().getAddress()),
                () -> assertEquals("+573009876543",    captor.getValue().getPhone()),
                () -> assertEquals("norte@mail.com",   captor.getValue().getEmail())
        );
    }

    @Test
    void testUpdateCinemaOnlyNonNullFields() throws Exception {
        // Arrange
        UpdateCinemaRequest request = new UpdateCinemaRequest("Cinepolis Norte", null, null, null);

        ArgumentCaptor<Cinema> captor = ArgumentCaptor.forClass(Cinema.class);
        Cinema existing = buildCinema("Cinepolis Centro");
        existing.setAddress("Calle Original 1");
        existing.setPhone("+570000000000");
        existing.setEmail("original@mail.com");

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(existing));
        when(cinemaRepository.save(any(Cinema.class))).thenReturn(existing);

        // Act
        cinemaService.updateCinema(CINEMA_ID, request);

        // Assert
        assertAll(
                () -> verify(cinemaRepository).save(captor.capture()),
                () -> assertEquals("Cinepolis Norte",  captor.getValue().getName()),
                () -> assertEquals("Calle Original 1", captor.getValue().getAddress()),
                () -> assertEquals("+570000000000",    captor.getValue().getPhone()),
                () -> assertEquals("original@mail.com",captor.getValue().getEmail())
        );
    }

    @Test
    void testUpdateCinemaWhenNotFound() {
        // Arrange
        UpdateCinemaRequest request = new UpdateCinemaRequest("Nuevo Nombre", null, null, null);

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> cinemaService.updateCinema(CINEMA_ID, request));
        verify(cinemaRepository, never()).save(any());
    }

    @Test
    void testFindAll() {
        // Arrange
        Cinema c1 = buildCinema("Cinepolis Centro");
        Cinema c2 = buildCinema("Cinepolis Norte");
        c2.setId(UUID.randomUUID());

        when(cinemaRepository.findAll()).thenReturn(List.of(c1, c2));

        // Act
        List<CinemaSummaryResponse> result = cinemaService.findAll();

        // Assert
        assertAll(
                () -> assertEquals(2,                  result.size()),
                () -> assertEquals("Cinepolis Centro", result.get(0).getName()),
                () -> assertEquals("Cinepolis Norte",  result.get(1).getName())
        );
    }

    @Test
    void testFindAllEmpty() {
        // Arrange
        when(cinemaRepository.findAll()).thenReturn(List.of());

        // Act
        List<CinemaSummaryResponse> result = cinemaService.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByAdminCinemaId() throws Exception {
        // Arrange
        Cinema cinema = buildCinema("Cinepolis Centro");
        when(cinemaRepository.findByAdminCinemaId(ADMIN_ID)).thenReturn(Optional.of(cinema));

        // Act
        CinemaResponse result = cinemaService.getByAdminCinemaId(ADMIN_ID);

        // Assert
        assertAll(
                () -> assertEquals(CINEMA_ID,  result.getId()),
                () -> assertEquals(COUNTRY_ID, result.getCountryId()),
                () -> assertEquals("Cinepolis Centro", result.getName())
        );
    }

    @Test
    void testGetByAdminCinemaIdNotFound() {
        // Arrange
        when(cinemaRepository.findByAdminCinemaId(ADMIN_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> cinemaService.getByAdminCinemaId(ADMIN_ID));
    }

    private Cinema buildCinema(String name) {
        Cinema c = new Cinema();
        c.setId(CINEMA_ID);
        c.setAdminCinemaId(ADMIN_ID);
        c.setCountryId(COUNTRY_ID);
        c.setName(name);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    private GlobalCost buildGlobalCost(BigDecimal dailyCost) {
        GlobalCost gc = new GlobalCost();
        gc.setId(UUID.randomUUID());
        gc.setDailyCost(dailyCost);
        gc.setEffectiveFrom(LocalDate.now());
        return gc;
    }
}
