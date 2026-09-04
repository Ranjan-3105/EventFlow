# 22. Kafka & Asynchronous Notifications

## Concept: Asynchronous Delivery

When a user successfully registers or pays for a booking, we want to send them an email notification. 

Why not send the email synchronously inside the `register` or `verifyPayment` HTTP request?
1. **Latency:** Connecting to an external SMTP server (Brevo) takes time. We don't want the user waiting on a loading screen for the email to send.
2. **Reliability:** If the SMTP server is temporarily down, the email sending would fail. If it was synchronous, the entire registration or payment transaction might roll back or return a 500 error, which is a terrible user experience for something secondary like a notification.

To solve this, EventFlow uses **Kafka** for asynchronous event-driven notifications.

## The Event-Driven Flow

```text
Business Operation (e.g., Payment Verification)
        |
        | 1. Success! (PostgreSQL committed)
        | 2. Produce Kafka Event
        ↓
Kafka Topic (e.g., "booking.confirmed")
        |
        | 3. Asynchronous buffering
        ↓
NotificationConsumer (@KafkaListener)
        |
        | 4. Consume Event
        | 5. Invoke EmailService
        ↓
Brevo SMTP
        |
        | 6. Send Email
        ↓
User Inbox
```

## Implementation

### 1. Producing Events

In `PaymentService`, after successfully verifying a payment and updating the database, we produce a `BookingConfirmedEvent`:

```java
// PaymentService.java
BookingConfirmedEvent event = new BookingConfirmedEvent(
        booking.getId(),
        booking.getUser().getId(),
        booking.getUser().getEmail(),
        booking.getEvent().getTitle(),
        seatLabels,
        booking.getTotalAmount(),
        Instant.now()
);

kafkaEventProducer.publishBookingConfirmed(event);
```

The `KafkaEventProducer` simply pushes this record to the `booking.confirmed` topic using Spring's `KafkaTemplate`.

### 2. Consuming Events

The `NotificationConsumer` runs in the background and listens to topics. 

```java
// NotificationConsumer.java
@KafkaListener(
        topics = "booking.confirmed",
        groupId = "eventflow-notifications"
)
public void handleBookingConfirmed(BookingConfirmedEvent event) {
    emailService.sendBookingConfirmationEmail(event);
}
```

By defining a `groupId`, we ensure that if we scale out to multiple backend instances in the future, only one instance will process a specific message (acting as a consumer group).

### 3. Sending the Email

The `EmailService` uses Spring's `JavaMailSender` (configured to use Brevo SMTP via `application.properties`) to construct and send a rich HTML email.

```java
MimeMessage message = mailSender.createMimeMessage();
MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

helper.setFrom("imvanguard2005@gmail.com");
helper.setTo(event.email());
helper.setSubject("Booking Confirmed • EventFlow");
helper.setText(html, true);

mailSender.send(message);
```

## What guarantees do we provide?

Currently, EventFlow provides **at-most-once / best-effort** delivery. 
If the application crashes immediately after the database commit but before the Kafka message is published, the email will not be sent. For true "at-least-once" delivery, we would need to implement the Outbox Pattern (saving the event to PostgreSQL in the same transaction, then having a background worker publish it to Kafka). However, for simple notifications, best-effort is sufficient and keeps the architecture lean.
