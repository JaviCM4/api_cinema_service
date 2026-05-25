package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateCinemaRequest;
import com.example.cinema.dtos.cinema.request.CreateCompanyRequest;
import com.example.cinema.dtos.cinema.request.UpdateCinemaRequest;
import com.example.cinema.dtos.cinema.response.CinemaResponse;
import com.example.cinema.dtos.cinema.response.CinemaSummaryResponse;
import com.example.cinema.dtos.cinema.response.CompanyResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.cinema.CinemaWallet;
import com.example.cinema.models.cinema.Company;
import com.example.cinema.models.cinema.GlobalCost;
import com.example.cinema.models.cinema.OperatingCost;
import com.example.cinema.repositories.cinema.CinemaRepository;
import com.example.cinema.repositories.cinema.CinemaWalletRepository;
import com.example.cinema.repositories.cinema.CompanyRepository;
import com.example.cinema.repositories.cinema.GlobalCostRepository;
import com.example.cinema.repositories.cinema.OperatingCostRepository;
import com.example.cinema.services.cinema.inteface.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CinemaServiceImplementation implements CinemaService {

    private final CinemaRepository cinemaRepository;
    private final CompanyRepository companyRepository;
    private final CinemaWalletRepository cinemaWalletRepository;
    private final OperatingCostRepository operatingCostRepository;
    private final GlobalCostRepository globalCostRepository;

    @Autowired
    public CinemaServiceImplementation(
            CinemaRepository cinemaRepository,
            CompanyRepository companyRepository,
            CinemaWalletRepository cinemaWalletRepository,
            OperatingCostRepository operatingCostRepository,
            GlobalCostRepository globalCostRepository
    ) {
        this.cinemaRepository = cinemaRepository;
        this.companyRepository = companyRepository;
        this.cinemaWalletRepository = cinemaWalletRepository;
        this.operatingCostRepository = operatingCostRepository;
        this.globalCostRepository = globalCostRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanyResponse createCompany(CreateCompanyRequest request) throws ConflictException {
        String normalizedName = request.getName().trim();
        if (companyRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("La empresa ya existe");
        }

        Company company = new Company();
        company.setName(normalizedName);
        return CompanyResponse.from(companyRepository.save(company));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> listCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(CompanyResponse::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCinema(CreateCinemaRequest dto) throws ResourceNotFoundException, ConflictException {
        GlobalCost globalCost = globalCostRepository.findFirstByOrderByEffectiveFromDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No hay un costo global registrado"));

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + dto.getCompanyId()));

        UUID adminCinemaId = dto.getAdminCinemaId();
        if (adminCinemaId != null) {
            Optional<Cinema> assigned = cinemaRepository.findByAdminCinemaId(adminCinemaId);
            if (assigned.isPresent()) {
                throw new ConflictException("El administrador ya esta asignado a otro cine");
            }
        }

        Cinema cinema = dto.createEntity();
        cinema.setCompany(company);
        Cinema savedCinema = cinemaRepository.save(cinema);

        CinemaWallet wallet = new CinemaWallet();
        wallet.setCinema(savedCinema);
        wallet.setBalance(BigDecimal.ZERO);
        cinemaWalletRepository.save(wallet);

        OperatingCost operatingCost = new OperatingCost();
        operatingCost.setCinema(savedCinema);
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
    @Transactional(rollbackFor = Exception.class)
    public void assignCinemaAdmin(UUID cinemaId, UUID adminCinemaId) throws ResourceNotFoundException, ConflictException {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cine no encontrado con id: " + cinemaId));

        Optional<Cinema> assigned = cinemaRepository.findByAdminCinemaId(adminCinemaId);
        if (assigned.isPresent() && !assigned.get().getId().equals(cinemaId)) {
            throw new ConflictException("El administrador ya esta asignado a otro cine");
        }

        cinema.setAdminCinemaId(adminCinemaId);
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
                .map(CinemaResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro un cine para el admin con id: " + adminCinemaId));
    }
}
