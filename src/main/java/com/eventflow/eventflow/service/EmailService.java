package com.eventflow.eventflow.service;

import com.eventflow.eventflow.kafka.event.BookingConfirmedEvent;
import com.eventflow.eventflow.kafka.event.UserRegisteredEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

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

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("imvanguard2005@gmail.com");
            helper.setTo(event.email());
            helper.setSubject("Booking Confirmed • EventFlow");

            String seats = String.join(", ", event.seats());

            String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>

                <body style="
                    margin: 0;
                    padding: 0;
                    background-color: #f4f6f8;
                    font-family: Arial, Helvetica, sans-serif;
                    color: #1f2937;
                ">

                    <div style="
                        max-width: 560px;
                        margin: 40px auto;
                        background: #ffffff;
                        border-radius: 12px;
                        overflow: hidden;
                        box-shadow: 0 4px 18px rgba(0,0,0,0.08);
                    ">

                        <!-- Header -->
                        <div style="
                            background: #111827;
                            padding: 24px 30px;
                            text-align: center;
                        ">
                            <div style="
                                color: #ffffff;
                                font-size: 24px;
                                font-weight: bold;
                                letter-spacing: 1px;
                            ">
                                EVENTFLOW
                            </div>

                            <div style="
                                margin-top: 6px;
                                color: #9ca3af;
                                font-size: 13px;
                            ">
                                Your ticket is confirmed
                            </div>
                        </div>

                        <!-- Content -->
                        <div style="padding: 32px;">

                            <div style="
                                display: inline-block;
                                padding: 7px 12px;
                                background: #ecfdf5;
                                color: #047857;
                                border-radius: 20px;
                                font-size: 13px;
                                font-weight: bold;
                            ">
                                ✓ BOOKING CONFIRMED
                            </div>

                            <h1 style="
                                margin: 22px 0 8px;
                                font-size: 24px;
                                line-height: 1.3;
                                color: #111827;
                            ">
                                %s
                            </h1>

                            <p style="
                                margin: 0 0 28px;
                                color: #6b7280;
                                font-size: 14px;
                            ">
                                Your booking has been successfully confirmed.
                            </p>

                            <!-- Booking details -->
                            <div style="
                                background: #f9fafb;
                                border: 1px solid #e5e7eb;
                                border-radius: 10px;
                                padding: 20px;
                            ">

                                <div style="margin-bottom: 18px;">
                                    <div style="
                                        color: #9ca3af;
                                        font-size: 11px;
                                        font-weight: bold;
                                        text-transform: uppercase;
                                        letter-spacing: 0.8px;
                                    ">
                                        Your seats
                                    </div>

                                    <div style="
                                        margin-top: 5px;
                                        font-size: 17px;
                                        font-weight: bold;
                                        color: #111827;
                                    ">
                                        %s
                                    </div>
                                </div>

                                <div style="
                                    border-top: 1px solid #e5e7eb;
                                    padding-top: 16px;
                                ">
                                    <div style="
                                        color: #9ca3af;
                                        font-size: 11px;
                                        font-weight: bold;
                                        text-transform: uppercase;
                                        letter-spacing: 0.8px;
                                    ">
                                        Total paid
                                    </div>

                                    <div style="
                                        margin-top: 5px;
                                        font-size: 20px;
                                        font-weight: bold;
                                        color: #111827;
                                    ">
                                        ₹%s
                                    </div>
                                </div>

                            </div>

                            <!-- Booking ID -->
                            <div style="
                                margin-top: 22px;
                                padding: 14px 16px;
                                background: #f8fafc;
                                border-radius: 8px;
                            ">
                                <div style="
                                    color: #9ca3af;
                                    font-size: 11px;
                                    font-weight: bold;
                                    text-transform: uppercase;
                                    letter-spacing: 0.8px;
                                ">
                                    Booking ID
                                </div>

                                <div style="
                                    margin-top: 5px;
                                    color: #374151;
                                    font-size: 12px;
                                    word-break: break-all;
                                ">
                                    %s
                                </div>
                            </div>

                            <p style="
                                margin: 28px 0 0;
                                color: #6b7280;
                                font-size: 13px;
                                line-height: 1.6;
                            ">
                                Thank you for choosing EventFlow.
                                We hope you enjoy the show!
                            </p>

                        </div>

                        <!-- Footer -->
                        <div style="
                            padding: 18px 30px;
                            background: #f9fafb;
                            border-top: 1px solid #e5e7eb;
                            text-align: center;
                            color: #9ca3af;
                            font-size: 11px;
                        ">
                            © 2026 EventFlow · This is an automated email.
                        </div>

                    </div>

                </body>
                </html>
                """.formatted(
                    event.eventName(),
                    seats,
                    event.amount().setScale(2),
                    event.bookingId()
            );

            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(
                    "Failed to send booking confirmation email", e
            );
        }
    }
}