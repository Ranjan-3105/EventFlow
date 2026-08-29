package com.eventflow.eventflow.service;

import com.eventflow.eventflow.kafka.event.BookingConfirmedEvent;
import com.eventflow.eventflow.kafka.event.UserRegisteredEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(UserRegisteredEvent event) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("imvanguard2005@gmail.com");
        message.setTo(event.email());
        message.setSubject("Welcome to EventFlow!");

        message.setText(
                "Hello " + event.firstName() + ",\n\n" +
                        "Welcome to EventFlow!\n\n" +
                        "Your account has been successfully created.\n\n" +
                        "Happy booking!\n\n" +
                        "— EventFlow"
        );

        mailSender.send(message);
    }

    public void sendBookingConfirmationEmail(BookingConfirmedEvent event) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("imvanguard2005@gmail.com");
        message.setTo(event.email());
        message.setSubject("Booking Confirmed - EventFlow");

        message.setText(
                "Your booking for " + event.eventName() + " is confirmed!\n\n" +
                "Booking ID: " + event.bookingId() + "\n" +
                "Seats: " + String.join(", ", event.seats()) + "\n" +
                "Total Amount: INR " + event.amount() + "\n\n" +
                "Thank you for using EventFlow!"
        );

        mailSender.send(message);
    }
}