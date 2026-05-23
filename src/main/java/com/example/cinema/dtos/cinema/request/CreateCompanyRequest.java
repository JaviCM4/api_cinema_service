package com.example.cinema.dtos.cinema.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class CreateCompanyRequest {

    @NotBlank(message = "El nombre de la empresa es requerido")
    @Size(min = 2, max = 255)
    String name;
}
