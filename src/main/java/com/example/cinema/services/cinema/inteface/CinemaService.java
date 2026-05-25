package com.example.cinema.services.cinema.inteface;

import com.example.cinema.dtos.cinema.request.CreateCinemaRequest;
import com.example.cinema.dtos.cinema.request.CreateCompanyRequest;
import com.example.cinema.dtos.cinema.request.UpdateCinemaRequest;
import com.example.cinema.dtos.cinema.response.CinemaResponse;
import com.example.cinema.dtos.cinema.response.CinemaSummaryResponse;
import com.example.cinema.dtos.cinema.response.CompanyResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface CinemaService {

    CompanyResponse createCompany(CreateCompanyRequest request) throws ConflictException;

    List<CompanyResponse> listCompanies();

    void createCinema(CreateCinemaRequest dto) throws ResourceNotFoundException, ConflictException;

    void updateCinema(UUID cinemaId, UpdateCinemaRequest dto) throws ResourceNotFoundException;

    void assignCinemaAdmin(UUID cinemaId, UUID adminCinemaId) throws ResourceNotFoundException, ConflictException;

    List<CinemaSummaryResponse> findAll();

    CinemaResponse getByAdminCinemaId(UUID adminCinemaId) throws ResourceNotFoundException;
}
