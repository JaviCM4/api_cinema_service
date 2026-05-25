package com.example.cinema.dtos.cinema.request;

import jakarta.validation.constraints.*;
import lombok.Value;

@Value
public class UpdateCinemaRequest {

    @Size(min = 2, max = 255)
    String name;

    @Size(max = 500)
    String address;

    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{8,20}$", message = "Formato de número de teléfono inválido")
    String phone;

    @Email
    @Size(max = 255)
    String email;
}
