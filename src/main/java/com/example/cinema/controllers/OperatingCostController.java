package com.example.cinema.controllers;

import com.example.cinema.dtos.cinema.request.CreateOperatingCostRequest;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.cinema.inteface.OperatingCostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/operating-costs")
public class OperatingCostController {

    private final OperatingCostService operatingCostService;

    @Autowired
    public OperatingCostController(OperatingCostService operatingCostService) {
        this.operatingCostService = operatingCostService;
    }

    @PostMapping
    public ResponseEntity<Void> createOperatingCost(@Valid @RequestBody CreateOperatingCostRequest request)
            throws ResourceNotFoundException, ConflictException {
        operatingCostService.createOperatingCost(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
