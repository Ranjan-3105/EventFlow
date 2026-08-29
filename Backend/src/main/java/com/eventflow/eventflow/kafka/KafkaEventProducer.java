package com.eventflow.eventflow.kafka;

import com.eventflow.eventflow.kafka.event.BookingConfirmedEvent;
import com.eventflow.eventflow.kafka.event.EventPublishedEvent;
import com.eventflow.eventflow.kafka.event.UserRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegistered(
            UserRegisteredEvent event
    ) {
        kafkaTemplate.send(
                "user.registered",
                event.userId().toString(),
                event
        );
    }

    public void publishBookingConfirmed(
            BookingConfirmedEvent event
    ) {
        kafkaTemplate.send(
                "booking.confirmed",
                event.bookingId().toString(),
                event
        );
    }

    public void publishEventPublished(
            EventPublishedEvent event
    ) {
        kafkaTemplate.send(
                "event.published",
                event.eventId().toString(),
                event
        );
    }
}