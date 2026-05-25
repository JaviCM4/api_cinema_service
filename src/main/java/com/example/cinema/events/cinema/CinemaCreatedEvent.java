package com.example.cinema.events.cinema;

import com.example.cinema.models.cinema.Cinema;
import lombok.Value;

import java.util.UUID;

@Value
public class CinemaCreatedEvent {
    String event;
    String id;
    String name;
    String phone;
    String companyName;

    public Cinema fromEvent(){
        Cinema cinema = new Cinema();
        cinema.setAdminCinemaId(UUID.fromString(id));
        cinema.setName(companyName);
        cinema.setPhone(phone);
        cinema.setCountryId(UUID.randomUUID());
        return cinema;
    }
}
