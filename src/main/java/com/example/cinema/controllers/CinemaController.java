package com.example.cinema.controllers;

import com.example.cinema.dtos.cinema.request.AssignCinemaAdminRequest;
import com.example.cinema.dtos.cinema.request.CreateCinemaRequest;
import com.example.cinema.dtos.cinema.request.CreateCompanyRequest;
import com.example.cinema.dtos.cinema.request.UpdateCinemaRequest;
import com.example.cinema.dtos.cinema.response.CinemaResponse;
import com.example.cinema.dtos.cinema.response.CinemaSummaryResponse;
import com.example.cinema.dtos.cinema.response.CompanyResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.cinema.inteface.CinemaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/cinemas")
public class CinemaController {

    private final CinemaService cinemaService;

    @Autowired
    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<CinemaSummaryResponse>> getAllCinemas() {
        return ResponseEntity.ok(cinemaService.findAll());
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping("/companies")
    public ResponseEntity<List<CompanyResponse>> getCompanies() {
        return ResponseEntity.ok(cinemaService.listCompanies());
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping("/companies")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request)
            throws ConflictException {
        return ResponseEntity.status(HttpStatus.CREATED).body(cinemaService.createCompany(request));
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','CINEMA_ADMIN')")
    @GetMapping("/admin/{adminCinemaId}")
    public ResponseEntity<CinemaResponse> getCinemaByAdmin(@PathVariable UUID adminCinemaId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(cinemaService.getByAdminCinemaId(adminCinemaId));
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping
    public ResponseEntity<Void> createCinema(@Valid @RequestBody CreateCinemaRequest request)
            throws ResourceNotFoundException, ConflictException {
        cinemaService.createCinema(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','CINEMA_ADMIN')")
    @PatchMapping("/{cinemaId}")
    public ResponseEntity<Void> updateCinema(@PathVariable UUID cinemaId, @Valid @RequestBody UpdateCinemaRequest request)
            throws ResourceNotFoundException {
        cinemaService.updateCinema(cinemaId, request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PatchMapping("/{cinemaId}/admin")
    public ResponseEntity<Void> assignCinemaAdmin(
            @PathVariable UUID cinemaId,
            @Valid @RequestBody AssignCinemaAdminRequest request
    ) throws ResourceNotFoundException, ConflictException {
        cinemaService.assignCinemaAdmin(cinemaId, request.getAdminCinemaId());
        return ResponseEntity.noContent().build();
    }
}
