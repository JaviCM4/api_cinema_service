package com.example.cinema.dtos.cinema.request;

import com.example.cinema.models.cinema.Cinema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class CreateCinemaRequest {

    @NotNull(message = "El ID de la empresa es requerido")
    UUID companyId;

    UUID adminCinemaId;

    @NotNull(message = "El ID del pais es requerido")
    UUID countryId;

    @NotBlank(message = "El nombre del cine es requerido")
    @Size(min = 2, max = 255)
    String name;

    @Size(max = 500)
    String address;

    @Pattern(regexp = "^[+]?[0-9\\s\\-().]{8,20}$", message = "Formato de numero de telefono invalido")
    String phone;

    @Email
    @Size(max = 255)
    String email;

    @NotNull
    @PastOrPresent
    LocalDate effectiveFrom;

    public Cinema createEntity() {
        Cinema cinema = new Cinema();
        cinema.setAdminCinemaId(adminCinemaId);
        cinema.setCountryId(countryId);
        cinema.setName(name.trim());
        cinema.setAddress(address != null ? address.trim() : null);
        cinema.setPhone(phone != null ? phone.trim() : null);
        cinema.setEmail(email != null ? email.trim().toLowerCase() : null);
        return cinema;
    }
}
