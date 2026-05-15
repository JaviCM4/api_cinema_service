package com.example.cinema.services.adblock;

import com.example.cinema.dtos.adblock.AdBlockPricingRequest;
import com.example.cinema.dtos.adblock.AdBlockPricingResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.AdBlockPricing;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.repositories.cinema.AdBlockPricingRepository;
import com.example.cinema.repositories.cinema.CinemaRepository;
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
public class AdBlockPricingServiceImplTest {

    // ----------------- Constants---------------------------

    private static final UUID       ID_CINEMA      = UUID.randomUUID();
    private static final UUID       ID_PRICING     = UUID.randomUUID();
    private static final BigDecimal PRICE_PER_DAY  = new BigDecimal("5.00");
    private static final BigDecimal NEW_PRICE      = new BigDecimal("10.00");
    private static final String     CINEMA_NAME    = "Cine Central";
    private static final String     CINEMA_ADDRESS = "Zona 1, Guatemala";

    // --------------------------- Mocks ---------------------------

    @Mock private AdBlockPricingRepository adBlockPricingRepository;
    @Mock private CinemaRepository         cinemaRepository;

    @InjectMocks
    private AdBlockPricingServiceImpl service;

    // --------------------------- Helpers ---------------------------

    private Cinema cinema() {
        Cinema cinema = new Cinema();
        cinema.setId(ID_CINEMA);
        cinema.setName(CINEMA_NAME);
        cinema.setAddress(CINEMA_ADDRESS);
        return cinema;
    }

    private AdBlockPricing adBlockPricing() {
        AdBlockPricing pricing = new AdBlockPricing();
        pricing.setId(ID_PRICING);
        pricing.setCinema(cinema());
        pricing.setPricePerDay(PRICE_PER_DAY);
        pricing.setUpdatedAt(LocalDateTime.now());
        return pricing;
    }

    private AdBlockPricingRequest request(BigDecimal price) {
        return new AdBlockPricingRequest(price);
    }

    // ---------------------------
    // getAdBlockPricing
    // ---------------------------

    @Test
    void getAdBlockPricing_WhenPricingExists_ReturnsPricingResponse() throws ResourceNotFoundException {
        // Arrange
        when(adBlockPricingRepository.findByCinemaId(ID_CINEMA))
                .thenReturn(Optional.of(adBlockPricing()));

        // Act
        AdBlockPricingResponse result = service.getAdBlockPricing(ID_CINEMA);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(ID_PRICING,     result.getId()),
                () -> assertEquals(ID_CINEMA,      result.getCinemaId()),
                () -> assertEquals(CINEMA_NAME,    result.getCinemaName()),
                () -> assertEquals(CINEMA_ADDRESS, result.getCinemaLocation()),
                () -> assertEquals(PRICE_PER_DAY,  result.getPricePerDay())
        );
    }

    @Test
    void getAdBlockPricing_WhenPricingNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(adBlockPricingRepository.findByCinemaId(ID_CINEMA)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> service.getAdBlockPricing(ID_CINEMA)
        );
    }

    // ---------------------------
    // createAdBlockPricing
    // ---------------------------

    @Test
    void createAdBlockPricing_WhenValidRequest_CreatesPricingSuccessfully() throws Exception {
        // Arrange
        ArgumentCaptor<AdBlockPricing> captor = ArgumentCaptor.forClass(AdBlockPricing.class);

        when(cinemaRepository.findById(ID_CINEMA)).thenReturn(Optional.of(cinema()));
        when(adBlockPricingRepository.findByCinemaId(ID_CINEMA)).thenReturn(Optional.empty());
        when(adBlockPricingRepository.save(any())).thenReturn(adBlockPricing());

        // Act
        AdBlockPricingResponse result = service.createAdBlockPricing(ID_CINEMA, request(PRICE_PER_DAY));

        // Assert
        assertAll(
                () -> verify(adBlockPricingRepository).save(captor.capture()),
                () -> assertEquals(PRICE_PER_DAY, captor.getValue().getPricePerDay()),
                () -> assertEquals(ID_CINEMA,     captor.getValue().getCinema().getId()),
                () -> assertNotNull(result),
                () -> assertEquals(ID_PRICING,    result.getId())
        );
    }

    @Test
    void createAdBlockPricing_WhenCinemaNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(cinemaRepository.findById(ID_CINEMA)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> service.createAdBlockPricing(ID_CINEMA, request(PRICE_PER_DAY))
                ),
                () -> verify(adBlockPricingRepository, never()).save(any())
        );
    }

    @Test
    void createAdBlockPricing_WhenPricingAlreadyExists_ThrowsConflictException() {
        // Arrange
        when(cinemaRepository.findById(ID_CINEMA)).thenReturn(Optional.of(cinema()));
        when(adBlockPricingRepository.findByCinemaId(ID_CINEMA))
                .thenReturn(Optional.of(adBlockPricing()));

        // Act & Assert
        assertAll(
                () -> assertThrows(ConflictException.class,
                        () -> service.createAdBlockPricing(ID_CINEMA, request(PRICE_PER_DAY))
                ),
                () -> verify(adBlockPricingRepository, never()).save(any())
        );
    }

    // ---------------------------
    // updateAdBlockPricing
    // ---------------------------

    @Test
    void updateAdBlockPricing_WhenPricingExists_UpdatesSuccessfully() throws ResourceNotFoundException {
        // Arrange
        ArgumentCaptor<AdBlockPricing> captor = ArgumentCaptor.forClass(AdBlockPricing.class);
        AdBlockPricing updated = adBlockPricing();
        updated.setPricePerDay(NEW_PRICE);

        when(adBlockPricingRepository.findByCinemaId(ID_CINEMA))
                .thenReturn(Optional.of(adBlockPricing()));
        when(adBlockPricingRepository.save(any())).thenReturn(updated);

        // Act
        AdBlockPricingResponse result = service.updateAdBlockPricing(ID_CINEMA, request(NEW_PRICE));

        // Assert
        assertAll(
                () -> verify(adBlockPricingRepository).save(captor.capture()),
                () -> assertEquals(NEW_PRICE, captor.getValue().getPricePerDay()),
                () -> assertNotNull(result),
                () -> assertEquals(NEW_PRICE, result.getPricePerDay())
        );
    }

    @Test
    void updateAdBlockPricing_WhenPricingNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(adBlockPricingRepository.findByCinemaId(ID_CINEMA)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> service.updateAdBlockPricing(ID_CINEMA, request(NEW_PRICE))
                ),
                () -> verify(adBlockPricingRepository, never()).save(any())
        );
    }

    // ---------------------------
    // getAllAdBlockPricings
    // ---------------------------

    @Test
    void getAllAdBlockPricings_WhenPricingsExist_ReturnsAllPricings() {
        // Arrange
        when(adBlockPricingRepository.findAll())
                .thenReturn(List.of(adBlockPricing(), adBlockPricing()));

        // Act
        List<AdBlockPricingResponse> result = service.getAllAdBlockPricings();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size())
        );
    }

    @Test
    void getAllAdBlockPricings_WhenNoPricingsExist_ReturnsEmptyList() {
        // Arrange
        when(adBlockPricingRepository.findAll()).thenReturn(List.of());

        // Act
        List<AdBlockPricingResponse> result = service.getAllAdBlockPricings();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }
}