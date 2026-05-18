package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateOperatingCostRequest;
import com.example.cinema.dtos.cinema.response.CinemaOperatingCostSummaryResponse;
import com.example.cinema.dtos.cinema.response.OperatingCostDetailResponse;
import com.example.cinema.events.operatingcost.OperatingCostCreatedEvent;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.kafka.CinemaEventProducer;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.cinema.OperatingCost;
import com.example.cinema.repositories.cinema.CinemaRepository;
import com.example.cinema.repositories.cinema.OperatingCostRepository;
import com.example.cinema.services.cinema.inteface.OperatingCostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OperatingCostServiceImplementation implements OperatingCostService {

    private final OperatingCostRepository operatingCostRepository;
    private final CinemaRepository cinemaRepository;
    private final CinemaEventProducer eventProducer;

    @Autowired
    public OperatingCostServiceImplementation(OperatingCostRepository operatingCostRepository, CinemaRepository cinemaRepository, CinemaEventProducer eventProducer) {
        this.operatingCostRepository = operatingCostRepository;
        this.cinemaRepository = cinemaRepository;
        this.eventProducer = eventProducer;
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

        OperatingCost savedOperatingCost = operatingCostRepository.save(dto.createEntity(cinema));

        // Publicar evento de creacion de costo operativo
        OperatingCostCreatedEvent event = OperatingCostCreatedEvent.fromEntity(savedOperatingCost);
        eventProducer.publishOperatingCostCreated(event);
    }

    @Override
    public List<CinemaOperatingCostSummaryResponse> getAllOperatingCostSummaries() {
        List<Cinema> cinemas = cinemaRepository.findAll();
        LocalDate today = LocalDate.now();

        return cinemas.stream().map(cinema -> {
            List<OperatingCost> costs =
                    operatingCostRepository.findByCinema_IdOrderByEffectiveFromAsc(cinema.getId());
            List<OperatingCostDetailResponse> details = new ArrayList<>();
            BigDecimal totalCost = BigDecimal.ZERO;

            for (int i = 0; i < costs.size(); i++) {
                OperatingCost cost = costs.get(i);
                LocalDate end = (i + 1 < costs.size())
                        ? costs.get(i + 1).getEffectiveFrom()
                        : today;
                long activeDays = ChronoUnit.DAYS.between(cost.getEffectiveFrom(), end);
                BigDecimal periodCost = cost.getDailyCost().multiply(BigDecimal.valueOf(activeDays));
                totalCost = totalCost.add(periodCost);
                details.add(new OperatingCostDetailResponse(
                        cost.getDailyCost(), cost.getEffectiveFrom(), activeDays, periodCost));
            }

            return new CinemaOperatingCostSummaryResponse(
                    cinema.getId(), cinema.getName(), details, totalCost);
        }).collect(java.util.stream.Collectors.toList());
    }
}
