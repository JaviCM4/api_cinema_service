package com.example.cinema.kafka;


import com.example.cinema.events.adblock.AdBlockCreatedEvent;
import com.example.cinema.events.comments.*;
import com.example.cinema.events.operatingcost.OperatingCostCreatedEvent;
import com.example.cinema.events.ratings.*;
import com.example.cinema.events.showtimes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
@Service
public class CinemaEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(CinemaEventProducer.class);

    public CinemaEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private void publish(String topic, Object event) {
        try {
            kafkaTemplate.send(topic, event);
        } catch (Exception e) {
            log.error("Error al publicar evento en topic {}: {}", topic, e.getMessage());
        }
    }

    public void publisRoomCommentCreated(RoomCommentCreatedEvent event) {
        publish(Topics.ROOM_COMMENT_CREATED, event);
    }

    public void publishRoomCommentUpdated(RoomCommentUpdateEvent event) {
        publish(Topics.ROOM_COMMENT_UPDATED, event);
    }

    public void publishRoomCommentDeleted(RoomCommentDeleteEvent event) {
        publish(Topics.ROOM_COMMENT_DELETED, event);
    }

    public void publishRoomRatingCreated(RoomRatingCreatedEvent event) {
        publish(Topics.ROOM_RATING_CREATED, event);
    }

    public void publishRoomRatingUpdated(RoomRatingUpdatedEvent event) {
        publish(Topics.ROOM_RATING_UPDATED, event);
    }

    public void publishFunctionCreated(ShowtimeCreatedEvent event) {
        publish(Topics.SHOWTIME_CREATED, event);
    }

    public void publishFunctionUpdated(ShowtimeUpdatedEvent event) {
        publish(Topics.SHOWTIME_UPDATED, event);
    }

    public void publishAdBlockCreated(AdBlockCreatedEvent event) {
        publish(Topics.AD_BLOCK_CREATED, event);
    }

    public void publishOperatingCostCreated(OperatingCostCreatedEvent event) {
        publish(Topics.OPERATING_COST_CREATED, event);
    }
}