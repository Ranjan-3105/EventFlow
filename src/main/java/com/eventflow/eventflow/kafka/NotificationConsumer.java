package com.eventflow.eventflow.kafka;

import com.eventflow.eventflow.kafka.event.UserRegisteredEvent;
import com.eventflow.eventflow.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final EmailService emailService;

    public NotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = "user.registered",
            groupId = "eventflow-notifications"
    )
    public void handleUserRegistered(UserRegisteredEvent event) {

        emailService.sendWelcomeEmail(event);
    }
}