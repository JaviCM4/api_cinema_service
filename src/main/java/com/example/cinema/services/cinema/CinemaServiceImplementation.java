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
import com.example.cinema.services.cinema.inteface.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CinemaServiceImplementation implements CinemaService {

    private final CinemaRepository cinemaRepository;
    private final CinemaWalletRepository cinemaWalletRepository;
    private final OperatingCostRepository operatingCostRepository;
    private final GlobalCostRepository globalCostRepository;

    @Autowired
    public CinemaServiceImplementation(CinemaRepository cinemaRepository, CinemaWalletRepository cinemaWalletRepository, OperatingCostRepository operatingCostRepository, GlobalCostRepository globalCostRepository) {
        this.cinemaRepository = cinemaRepository;
        this.cinemaWalletRepository = cinemaWalletRepository;
        this.operatingCostRepository = operatingCostRepository;
        this.globalCostRepository = globalCostRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCinema(CreateCinemaRequest dto) throws ResourceNotFoundException {
        GlobalCost globalCost = globalCostRepository.findFirstByOrderByEffectiveFromDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No hay un costo global registrado"));

        Cinema cinema = cinemaRepository.save(dto.createEntity());

        CinemaWallet wallet = new CinemaWallet();
        wallet.setCinema(cinema);
        wallet.setBalance(BigDecimal.ZERO);
        cinemaWalletRepository.save(wallet);

        OperatingCost operatingCost = new OperatingCost();
        operatingCost.setCinema(cinema);
        operatingCost.setDailyCost(globalCost.getDailyCost());
        operatingCost.setEffectiveFrom(dto.getEffectiveFrom());
        operatingCostRepository.save(operatingCost);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCinema(UUID cinemaId, UpdateCinemaRequest dto)
            throws ResourceNotFoundException {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cine no encontrado con id: " + cinemaId));

        if (dto.getName() != null) cinema.setName(dto.getName().trim());
        if (dto.getAddress() != null) cinema.setAddress(dto.getAddress().trim());
        if (dto.getPhone() != null) cinema.setPhone(dto.getPhone().trim());
        if (dto.getEmail() != null) cinema.setEmail(dto.getEmail().trim().toLowerCase());

        cinemaRepository.save(cinema);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CinemaSummaryResponse> findAll() {
        return cinemaRepository.findAll()
                .stream()
                .map(CinemaSummaryResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CinemaResponse getByAdminCinemaId(UUID adminCinemaId) throws ResourceNotFoundException {
        return cinemaRepository.findByAdminCinemaId(adminCinemaId)
                .stream()
                .findFirst()
                .map(CinemaResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un cine para el admin con id: " + adminCinemaId));
    }

}
