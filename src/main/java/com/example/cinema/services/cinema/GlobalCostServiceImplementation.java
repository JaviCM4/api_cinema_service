package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateGlobalCostRequest;
import com.example.cinema.dtos.cinema.response.GlobalCostResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.GlobalCost;
import com.example.cinema.repositories.cinema.GlobalCostRepository;
import com.example.cinema.services.cinema.inteface.GlobalCostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GlobalCostServiceImplementation implements GlobalCostService {

    private final GlobalCostRepository globalCostRepository;

    @Autowired
    public GlobalCostServiceImplementation(GlobalCostRepository globalCostRepository) {
        this.globalCostRepository = globalCostRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createGlobalCost(CreateGlobalCostRequest dto) throws ConflictException {
        if (globalCostRepository.existsByEffectiveFrom(dto.getEffectiveFrom())) {
            throw new ConflictException("Ya existe un costo global para la fecha: " + dto.getEffectiveFrom());
        }
        globalCostRepository.save(dto.createEntity());
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalCostResponse getLatestGlobalCost() throws ResourceNotFoundException {
        return globalCostRepository.findFirstByOrderByEffectiveFromDesc()
                .map(GlobalCostResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No hay un costo global registrado"));
    }
}
