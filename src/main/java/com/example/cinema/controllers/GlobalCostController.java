package com.example.cinema.controllers;

import com.example.cinema.dtos.cinema.request.CreateGlobalCostRequest;
import com.example.cinema.dtos.cinema.response.GlobalCostResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.cinema.inteface.GlobalCostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/global-costs")
public class GlobalCostController {

    private final GlobalCostService globalCostService;

    @Autowired
    public GlobalCostController(GlobalCostService globalCostService) {
        this.globalCostService = globalCostService;
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping
    public ResponseEntity<Void> createGlobalCost(@Valid @RequestBody CreateGlobalCostRequest dto)
            throws ConflictException {
        globalCostService.createGlobalCost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @GetMapping
    public ResponseEntity<GlobalCostResponse> getLatest() throws ResourceNotFoundException {
        return ResponseEntity.ok(globalCostService.getLatestGlobalCost());
    }
}
