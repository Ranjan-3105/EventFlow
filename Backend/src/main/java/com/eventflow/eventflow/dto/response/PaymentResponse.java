package com.eventflow.eventflow.dto.response;

import com.eventflow.eventflow.entity.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(

        UUID paymentId,

        UUID bookingId,

        BigDecimal amount,

        PaymentStatus status,

        String razorpayOrderId,

        String razorpayKeyId

) {}