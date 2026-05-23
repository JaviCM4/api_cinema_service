package com.example.cinema.kafka;

import com.example.cinema.events.cinema.CinemaCreatedEvent;
import com.example.cinema.repositories.cinema.CinemaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CinemaEventConsumer {
    private static final String USER_EVENTS_TOPIC = "user-events";
    private final CinemaRepository cinemaRepository;
    private final ObjectMapper objectMapper;

    public CinemaEventConsumer(CinemaRepository cinemaRepository, ObjectMapper objectMapper) {
        this.cinemaRepository = cinemaRepository;
        this.objectMapper = objectMapper;
    }
/*
    @KafkaListener(topics = USER_EVENTS_TOPIC, groupId = "cinema-service-group")
    public void onAdvertiserCreated(String payload) throws JsonProcessingException {
        CinemaCreatedEvent event = objectMapper.readValue(payload, CinemaCreatedEvent.class);
        if ("CINEMA_ADMIN_CREATED".equals(event.getEvent())) {
            cinemaRepository.save(event.fromEvent());
        }
    }
*/
}
