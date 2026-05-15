package com.example.cinema.services.theater;

import com.example.cinema.dtos.theater.request.TheaterPrincingRequest;
import com.example.cinema.dtos.theater.response.TheaterPrincingResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.TheaterPricing;
import com.example.cinema.models.theater.TypeTheater;
import com.example.cinema.repositories.theater.TheaterPricingRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import com.example.cinema.repositories.theater.TypeTheaterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TheaterPricingServiceImplTest {

    // --------------------- Constants----------------

    private static final UUID       ID_THEATER      = UUID.randomUUID();
    private static final UUID       ID_TYPE_THEATER = UUID.randomUUID();
    private static final UUID       ID_PRICING      = UUID.randomUUID();
    private static final BigDecimal PRICE           = new BigDecimal("15.00");
    private static final LocalDate  EFFECTIVE_DATE  = LocalDate.now();

    // ---------------- Mocks ----------------

    @Mock private TheaterPricingRepository theaterPricingRepository;
    @Mock private TheaterRepository        theaterRepository;
    @Mock private TypeTheaterRepository    typeTheaterRepository;

    @InjectMocks
    private TheaterPricingImplementation service;

    // ---------------- Helpers ----------------

    private Theater theater() {
        Cinema cinema = new Cinema();
        cinema.setId(UUID.randomUUID());
        cinema.setName("Cine Central");

        Theater theater = new Theater();
        theater.setId(ID_THEATER);
        theater.setName("Sala VIP");
        theater.setCinema(cinema);
        return theater;
    }

    private TypeTheater typeTheater() {
        TypeTheater type = new TypeTheater();
        type.setId(ID_TYPE_THEATER);
        type.setName("VIP");
        return type;
    }

    private TheaterPricing theaterPricing() {
        TheaterPricing pricing = new TheaterPricing();
        pricing.setId(ID_PRICING);
        pricing.setTheater(theater());
        pricing.setTypeTheater(typeTheater());
        pricing.setPrice(PRICE);
        pricing.setEffectiveDate(EFFECTIVE_DATE);
        return pricing;
    }

    private TheaterPrincingRequest pricingRequest() {
        return new TheaterPrincingRequest(ID_TYPE_THEATER, PRICE, EFFECTIVE_DATE);
    }

    // ----------------
    // getTheaterPricing
    // ----------------

    @Test
    void getTheaterPricing_WhenPricingExists_ReturnsPricingResponse() throws Exception {
        // Arrange
        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.of(theater()));
        when(theaterPricingRepository.existsByTheaterId(ID_THEATER)).thenReturn(true);
        when(theaterPricingRepository.findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                eq(ID_THEATER), any(LocalDate.class)))
                .thenReturn(Optional.of(theaterPricing()));

        // Act
        TheaterPrincingResponse result = service.getTheaterPricing(ID_THEATER);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(ID_PRICING,      result.getTheaterPricingId()),
                () -> assertEquals(ID_THEATER,      result.getTheaterId()),
                () -> assertEquals(ID_TYPE_THEATER, result.getTypeTheaterId()),
                () -> assertEquals(PRICE,           result.getPrice()),
                () -> assertEquals(EFFECTIVE_DATE,  result.getEffectiveDate())
        );
    }

    @Test
    void getTheaterPricing_WhenTheaterNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> service.getTheaterPricing(ID_THEATER)
                ),
                () -> verify(theaterPricingRepository, never()).existsByTheaterId(any())
        );
    }

    @Test
    void getTheaterPricing_WhenNoPricingAssigned_ThrowsResourceNotFoundException() {
        // Arrange
        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.of(theater()));
        when(theaterPricingRepository.existsByTheaterId(ID_THEATER)).thenReturn(false);

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> service.getTheaterPricing(ID_THEATER)
                ),
                () -> verify(theaterPricingRepository, never())
                        .findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(any(), any())
        );
    }

    @Test
    void getTheaterPricing_WhenNoActivePricing_ThrowsResourceNotFoundException() {
        // Arrange
        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.of(theater()));
        when(theaterPricingRepository.existsByTheaterId(ID_THEATER)).thenReturn(true);
        when(theaterPricingRepository.findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                eq(ID_THEATER), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> service.getTheaterPricing(ID_THEATER)
        );
    }

    // ----------------
    // createTheaterPricing
    // ----------------

    @Test
    void createTheaterPricing_WhenValidRequest_CreatesPricingSuccessfully() throws Exception {
        // Arrange
        ArgumentCaptor<TheaterPricing> captor = ArgumentCaptor.forClass(TheaterPricing.class);

        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.of(theater()));
        when(typeTheaterRepository.findById(ID_TYPE_THEATER)).thenReturn(Optional.of(typeTheater()));
        when(theaterPricingRepository.findFirstByTheater_IdAndTypeTheater_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                eq(ID_THEATER), eq(ID_TYPE_THEATER), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(typeTheaterRepository.getReferenceById(ID_TYPE_THEATER)).thenReturn(typeTheater());
        when(theaterPricingRepository.save(any())).thenReturn(theaterPricing());

        // Act
        TheaterPrincingResponse result = service.createTheaterPricing(ID_THEATER, pricingRequest());

        // Assert
        assertAll(
                () -> verify(theaterPricingRepository).save(captor.capture()),
                () -> assertEquals(PRICE,          captor.getValue().getPrice()),
                () -> assertEquals(EFFECTIVE_DATE, captor.getValue().getEffectiveDate()),
                () -> assertNotNull(result),
                () -> assertEquals(ID_PRICING,     result.getTheaterPricingId())
        );
    }

    @Test
    void createTheaterPricing_WhenTheaterNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> service.createTheaterPricing(ID_THEATER, pricingRequest())
                ),
                () -> verify(theaterPricingRepository, never()).save(any())
        );
    }

    @Test
    void createTheaterPricing_WhenTypeTheaterNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.of(theater()));
        when(typeTheaterRepository.findById(ID_TYPE_THEATER)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> service.createTheaterPricing(ID_THEATER, pricingRequest())
                ),
                () -> verify(theaterPricingRepository, never()).save(any())
        );
    }

    @Test
    void createTheaterPricing_WhenActivePricingAlreadyExists_ThrowsConflictException() {
        // Arrange
        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.of(theater()));
        when(typeTheaterRepository.findById(ID_TYPE_THEATER)).thenReturn(Optional.of(typeTheater()));
        when(theaterPricingRepository.findFirstByTheater_IdAndTypeTheater_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                eq(ID_THEATER), eq(ID_TYPE_THEATER), any(LocalDate.class)))
                .thenReturn(Optional.of(theaterPricing()));

        // Act & Assert
        assertAll(
                () -> assertThrows(ConflictException.class,
                        () -> service.createTheaterPricing(ID_THEATER, pricingRequest())
                ),
                () -> verify(theaterPricingRepository, never()).save(any())
        );
    }

    // ----------------
    // updateTheaterPricing
    // ----------------

    @Test
    void updateTheaterPricing_WhenValidRequest_UpdatesPricingSuccessfully() throws Exception {
        // Arrange
        ArgumentCaptor<TheaterPricing> captor = ArgumentCaptor.forClass(TheaterPricing.class);
        BigDecimal newPrice = new BigDecimal("20.00");
        TheaterPrincingRequest updateRequest = new TheaterPrincingRequest(
                ID_TYPE_THEATER, newPrice, EFFECTIVE_DATE
        );
        TheaterPricing updated = theaterPricing();
        updated.setPrice(newPrice);

        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.of(theater()));
        when(typeTheaterRepository.findById(ID_TYPE_THEATER)).thenReturn(Optional.of(typeTheater()));
        when(theaterPricingRepository.findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                eq(ID_THEATER), any(LocalDate.class)))
                .thenReturn(Optional.of(theaterPricing()));
        when(typeTheaterRepository.getReferenceById(ID_TYPE_THEATER)).thenReturn(typeTheater());
        when(theaterPricingRepository.save(any())).thenReturn(updated);

        // Act
        TheaterPrincingResponse result = service.updateTheaterPricing(ID_THEATER, updateRequest);

        // Assert
        assertAll(
                () -> verify(theaterPricingRepository).save(captor.capture()),
                () -> assertEquals(newPrice,       captor.getValue().getPrice()),
                () -> assertEquals(EFFECTIVE_DATE, captor.getValue().getEffectiveDate()),
                () -> assertNotNull(result),
                () -> assertEquals(newPrice,       result.getPrice())
        );
    }

    @Test
    void updateTheaterPricing_WhenTheaterNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> service.updateTheaterPricing(ID_THEATER, pricingRequest())
                ),
                () -> verify(theaterPricingRepository, never()).save(any())
        );
    }

    @Test
    void updateTheaterPricing_WhenTypeTheaterNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.of(theater()));
        when(typeTheaterRepository.findById(ID_TYPE_THEATER)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> service.updateTheaterPricing(ID_THEATER, pricingRequest())
                ),
                () -> verify(theaterPricingRepository, never()).save(any())
        );
    }

    @Test
    void updateTheaterPricing_WhenNoActivePricing_ThrowsResourceNotFoundException() {
        // Arrange
        when(theaterRepository.findById(ID_THEATER)).thenReturn(Optional.of(theater()));
        when(typeTheaterRepository.findById(ID_TYPE_THEATER)).thenReturn(Optional.of(typeTheater()));
        when(theaterPricingRepository.findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                eq(ID_THEATER), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(ResourceNotFoundException.class,
                        () -> service.updateTheaterPricing(ID_THEATER, pricingRequest())
                ),
                () -> verify(theaterPricingRepository, never()).save(any())
        );
    }

    // ----------------
    // getPriceForTheater
    // ----------------

    @Test
    void getPriceForTheater_WhenActivePricingExists_ReturnsPrice() throws Exception {
        // Arrange
        when(theaterPricingRepository.findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                eq(ID_THEATER), any(LocalDate.class)))
                .thenReturn(Optional.of(theaterPricing()));

        // Act
        BigDecimal result = service.getPriceForTheater(ID_THEATER);

        // Assert
        assertEquals(PRICE, result);
    }

    @Test
    void getPriceForTheater_WhenNoActivePricing_ThrowsResourceNotFoundException() {
        // Arrange
        when(theaterPricingRepository.findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                eq(ID_THEATER), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> service.getPriceForTheater(ID_THEATER)
        );
    }
}