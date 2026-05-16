package com.example.cinema.kafka;


import com.example.cinema.events.adblock.AdBlockCreatedEvent;
import com.example.cinema.events.comments.*;
import com.example.cinema.events.operatingcost.OperatingCostCreatedEvent;
import com.example.cinema.events.ratings.*;
import com.example.cinema.events.showtimes.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CinemaEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public CinemaEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    //Eventos para comentarios
    public void publisRoomCommentCreated(RoomCommentCreatedEvent event) {
        kafkaTemplate.send(Topics.ROOM_COMMENT_CREATED, event);
    }

    public void publishRoomCommentUpdated(RoomCommentUpdateEvent event) {
        kafkaTemplate.send(Topics.ROOM_COMMENT_UPDATED, event);
    }

    public void publishRoomCommentDeleted(RoomCommentDeleteEvent event) {
        kafkaTemplate.send(Topics.ROOM_COMMENT_DELETED, event);
    }

    //Eventos para ratings
    public void publishRoomRatingCreated(RoomRatingCreatedEvent event) {
        kafkaTemplate.send(Topics.ROOM_RATING_CREATED, event);
    }

    public void publishRoomRatingUpdated(RoomRatingUpdatedEvent event) {
        kafkaTemplate.send(Topics.ROOM_RATING_UPDATED, event);
    }

    //Eventos para funciones
    public void publishFunctionCreated(ShowtimeCreatedEvent event) {
        kafkaTemplate.send(Topics.SHOWTIME_CREATED, event);
    }

    public void publishFunctionUpdated(ShowtimeUpdatedEvent event) {
        kafkaTemplate.send(Topics.SHOWTIME_UPDATED, event);
    }

    //Eventos para bloqueos de anuncios
    public void publishAdBlockCreated(AdBlockCreatedEvent event) {
        kafkaTemplate.send(Topics.AD_BLOCK_CREATED, event);
    }

    //Eventos para costos operativos
    public void publishOperatingCostCreated(OperatingCostCreatedEvent event) {
        kafkaTemplate.send(Topics.OPERATING_COST_CREATED, event);
    }

}
