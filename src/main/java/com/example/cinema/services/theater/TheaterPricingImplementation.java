package com.example.cinema.services.theater;

import com.example.cinema.dtos.theater.request.TheaterPrincingRequest;
import com.example.cinema.dtos.theater.response.TheaterPrincingResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.TheaterPricing;
import com.example.cinema.repositories.theater.TheaterPricingRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import com.example.cinema.repositories.theater.TypeTheaterRepository;
import com.example.cinema.services.theater.inteface.TheaterPricingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class TheaterPricingImplementation implements TheaterPricingService {
    private final TheaterPricingRepository theaterPricingRepository;
    private final TheaterRepository theaterRepository;
    private final TypeTheaterRepository typeTheaterRepository;

    public TheaterPricingImplementation(TheaterPricingRepository theaterPricingRepository, TheaterRepository theaterRepository, TypeTheaterRepository typeTheaterRepository) {
        this.theaterPricingRepository = theaterPricingRepository;
        this.theaterRepository = theaterRepository;
        this.typeTheaterRepository = typeTheaterRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TheaterPrincingResponse getTheaterPricing(UUID theaterId) throws ResourceNotFoundException, ConflictException {
        //Obtenemos la sala
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

        //Verificamos que la sala no tenga un precio asignado
        if (!theaterPricingRepository.existsByTheaterId(theaterId)) {
            throw new ResourceNotFoundException("No se ha asignado un precio a esta sala");
        }

        //Obtenemos el precio actual por la fecha disponible mas reciente
        TheaterPricing theaterPricing = theaterPricingRepository.findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(theaterId, LocalDate.now())
                    .orElseThrow(() -> new ResourceNotFoundException("No se ha asignado un precio vigente a esta sala"));

        return TheaterPrincingResponse.fromEntity(theaterPricing);
    }

    @Override
    public TheaterPrincingResponse createTheaterPricing(UUID theaterId, TheaterPrincingRequest dto) throws ResourceNotFoundException, ConflictException {
        //Obtenemos la sala
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

        //Verificamos que el tipo de sala exista
        typeTheaterRepository.findById(dto.getTypeTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de sala no encontrado"));

        //Verificamos que no exista un precio vigente para esta sala y tipo de sala
        if (theaterPricingRepository.findFirstByTheater_IdAndTypeTheater_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                theaterId, dto.getTypeTheaterId(), LocalDate.now()).isPresent()) {
            throw new ConflictException("Ya existe un precio vigente para esta sala y tipo de sala");
        }

        TheaterPricing theaterPricing = new TheaterPricing();
        theaterPricing.setTheater(theater);
        theaterPricing.setTypeTheater(typeTheaterRepository.getReferenceById(dto.getTypeTheaterId()));
        theaterPricing.setPrice(dto.getPrice());
        theaterPricing.setEffectiveDate(dto.getEffectiveDate());

        TheaterPricing saved = theaterPricingRepository.save(theaterPricing);
        return TheaterPrincingResponse.fromEntity(saved);
    }

    @Override
    public TheaterPrincingResponse updateTheaterPricing(UUID theaterId, TheaterPrincingRequest dto) throws ResourceNotFoundException {
        //Obtenemos la sala
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

        //Verificamos que el tipo de sala exista
        typeTheaterRepository.findById(dto.getTypeTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de sala no encontrado"));

        //Obtenemos el precio actual por la fecha disponible mas reciente
        TheaterPricing theaterPricing = theaterPricingRepository.findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(theaterId, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("No se ha asignado un precio vigente a esta sala"));

        theaterPricing.setTypeTheater(typeTheaterRepository.getReferenceById(dto.getTypeTheaterId()));
        theaterPricing.setPrice(dto.getPrice());
        theaterPricing.setEffectiveDate(dto.getEffectiveDate());

        TheaterPricing saved = theaterPricingRepository.save(theaterPricing);
        return TheaterPrincingResponse.fromEntity(saved);
    }

    @Override
    public BigDecimal getPriceForTheater(UUID theaterId) throws ResourceNotFoundException {
        //Obtenemos el precio actual por la fecha disponible mas reciente
        TheaterPricing theaterPricing = theaterPricingRepository.findTopByTheaterIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(theaterId, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("No se ha asignado un precio vigente a esta sala"));

        return theaterPricing.getPrice();
    }
}
