# 21. Payment Module

## Concept: Separation of Booking and Payment

In EventFlow, **Booking** and **Payment** are separate but related concepts. 

Why separate them?
Because a user must reserve seats _before_ they are redirected to a payment gateway. If payment fails, we need a way to track the failed attempt and eventually release the seats. If payment succeeds, we need to verify it securely before permanently assigning the seats.

## Business Rules

1. A Booking is created in a `PENDING` state.
2. Only `PENDING` bookings can be paid for.
3. A Razorpay Order is created representing the payment intent.
4. The frontend processes the payment directly with Razorpay.
5. The frontend sends a verification request back to the backend.
6. The backend **must not** trust the frontend's claim of success. It must mathematically verify the Razorpay signature (`razorpay_signature`).
7. Upon successful verification:
   - Payment status becomes `SUCCESS`.
   - Booking status becomes `CONFIRMED`.
   - Redis seat locks are released (since the seats are now permanently persisted in PostgreSQL).
   - A Kafka event is published.

## The Payment Flow

```text
Frontend                        Backend (PaymentController)              Razorpay
   |                                      |                                  |
   | --- 1. POST /api/payments ---------> |                                  |
   |     { bookingId: ... }               |                                  |
   |                                      | --- 2. Create Order -----------> |
   |                                      | <------ 3. Order ID (order_x) -- |
   | <--- 4. PaymentResponse (order_x) -- |                                  |
   |                                      |                                  |
   | --- 5. Razorpay Checkout (order_x) -----------------------------------> |
   | <--- 6. Payment Success (payment_y, signature_z) ---------------------- |
   |                                      |                                  |
   | --- 7. POST /api/payments/verify --> |                                  |
   |     { orderId, paymentId, sig }      |                                  |
   |                                      | --- 8. Signature Verification    |
   |                                      | --- 9. Booking = CONFIRMED       |
   |                                      | --- 10. Payment = SUCCESS        |
   | <--- 11. 200 OK -------------------- |                                  |
```

## Implementation: `PaymentService`

### 1. Creating the Razorpay Order

When the user initiates payment, we check if the booking is valid and pending. Then we create an order via the `RazorpayClient`.

```java
// PaymentService.java
JSONObject orderRequest = new JSONObject();
orderRequest.put("amount", amountInPaise);
orderRequest.put("currency", "INR");
orderRequest.put("receipt", booking.getId().toString());

Order razorpayOrder = razorpayClient.orders.create(orderRequest);
String razorpayOrderId = razorpayOrder.get("id");

Payment payment = new Payment();
payment.setBooking(booking);
payment.setAmount(amount);
payment.setStatus(PaymentStatus.PENDING);
payment.setRazorpayOrderId(razorpayOrderId);

paymentRepository.save(payment);
```

### 2. Verifying the Payment

When the frontend reports success, we mathematically verify the signature using our `razorpay.key-secret`. 

```java
// PaymentService.java
JSONObject attributes = new JSONObject();
attributes.put("razorpay_order_id", request.razorpayOrderId());
attributes.put("razorpay_payment_id", request.razorpayPaymentId());
attributes.put("razorpay_signature", request.razorpaySignature());

boolean valid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

if (!valid) {
    throw new PaymentException("Invalid payment signature.");
}
```

If valid, we update the state:
```java
payment.setStatus(PaymentStatus.SUCCESS);
booking.setStatus(BookingStatus.CONFIRMED);

paymentRepository.save(payment);
bookingRepository.save(booking);
```

### 3. Releasing Redis Locks safely

We only release the Redis seat locks *after* the PostgreSQL transaction successfully commits. We use `TransactionSynchronizationManager` for this.

```java
if (TransactionSynchronizationManager.isSynchronizationActive()) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (UUID seatId : seatIds) {
                    seatLockService.releaseLock(eventId, seatId, lockOwner);
                }
            }
        }
    );
}
```

## Idempotency

What if the frontend sends the verification request twice due to a network glitch?
The `verifyPayment` method handles idempotency:

```java
if (payment.getStatus() == PaymentStatus.SUCCESS) {
    return mapToResponse(payment); // Already verified
}
```

This prevents duplicate processing and errors when multiple requests attempt to confirm the same payment.
