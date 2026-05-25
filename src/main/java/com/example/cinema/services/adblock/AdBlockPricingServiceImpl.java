package com.example.cinema.services.adblock;

import com.example.cinema.dtos.adblock.AdBlockPricingRequest;
import com.example.cinema.dtos.adblock.AdBlockPricingResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.AdBlockPricing;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.repositories.cinema.AdBlockPricingRepository;
import com.example.cinema.repositories.cinema.CinemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdBlockPricingServiceImpl implements AdBlockPricingService{
    private final AdBlockPricingRepository adBlockPricingRepository;
    private final CinemaRepository cinemaRepository;

    public AdBlockPricingServiceImpl(AdBlockPricingRepository adBlockPricingRepository, CinemaRepository cinemaRepository) {
        this.adBlockPricingRepository = adBlockPricingRepository;
        this.cinemaRepository = cinemaRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public AdBlockPricingResponse getAdBlockPricing(UUID cinemaId) throws ResourceNotFoundException {
        //Obtenemos el precio de bloque publicitario para el cine dado su ID
        AdBlockPricing adBlockPricing = adBlockPricingRepository.findByCinemaId(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el precio para bloquear los anuncios publicitarios del cine"));

        return AdBlockPricingResponse.fromEntity(adBlockPricing);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AdBlockPricingResponse createAdBlockPricing(UUID cinemaId, AdBlockPricingRequest request) throws ResourceNotFoundException, ConflictException {
        //Verificamos que el cine exista
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cine para asignar el precio de bloque publicitario"));

        //Verificamos que no exista un precio de bloque publicitario para ese cine
        if (adBlockPricingRepository.findByCinemaId(cinemaId).isPresent()) {
            throw new ConflictException("Ya existe un precio de bloque publicitario para este cine");
        }

        //Creamos el nuevo precio de bloque publicitario
        AdBlockPricing adBlockPricing = new AdBlockPricing();
        adBlockPricing.setCinema(cinema);
        adBlockPricing.setPricePerDay(request.getPricePerDay());

        AdBlockPricing savedAdBlockPricing = adBlockPricingRepository.save(adBlockPricing);
        return AdBlockPricingResponse.fromEntity(savedAdBlockPricing);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AdBlockPricingResponse updateAdBlockPricing(UUID cinemaId, AdBlockPricingRequest request) throws ResourceNotFoundException {
        //Obtenemos el precio de bloque publicitario para el cine dado su ID
        AdBlockPricing adBlockPricing = adBlockPricingRepository.findByCinemaId(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el precio para bloquear los anuncios publicitarios del cine"));

        //Actualizamos el precio de bloque publicitario
        adBlockPricing.setPricePerDay(request.getPricePerDay());

        AdBlockPricing updatedAdBlockPricing = adBlockPricingRepository.save(adBlockPricing);
        return AdBlockPricingResponse.fromEntity(updatedAdBlockPricing);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AdBlockPricingResponse> getAllAdBlockPricings() {
        //Obtenemos todos los precios de bloque publicitario y los convertimos a respuestas DTO
        List<AdBlockPricing> adBlockPricings = adBlockPricingRepository.findAll();
        return adBlockPricings.stream()
                .map(AdBlockPricingResponse::fromEntity)
                .toList();
    }
}
