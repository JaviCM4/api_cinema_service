package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateOperatingCostRequest;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.repositories.cinema.CinemaRepository;
import com.example.cinema.repositories.cinema.OperatingCostRepository;
import com.example.cinema.services.cinema.inteface.OperatingCostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperatingCostServiceImplementation implements OperatingCostService {

    private final OperatingCostRepository operatingCostRepository;
    private final CinemaRepository cinemaRepository;

    @Autowired
    public OperatingCostServiceImplementation(OperatingCostRepository operatingCostRepository, CinemaRepository cinemaRepository) {
        this.operatingCostRepository = operatingCostRepository;
        this.cinemaRepository = cinemaRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOperatingCost(CreateOperatingCostRequest dto)
            throws ResourceNotFoundException, ConflictException {
        Cinema cinema = cinemaRepository.findById(dto.getCinemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cine no encontrado con id: " + dto.getCinemaId()));

        if (operatingCostRepository.existsByCinema_IdAndEffectiveFrom(dto.getCinemaId(), dto.getEffectiveFrom())) {
            throw new ConflictException("Ya existe un costo operativo para este cine en la fecha: " + dto.getEffectiveFrom());
        }

        operatingCostRepository.save(dto.createEntity(cinema));
    }
}
