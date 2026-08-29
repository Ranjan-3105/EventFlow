package com.eventflow.eventflow.dto.response;

import com.eventflow.eventflow.entity.BookingStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record BookingResponse(

        UUID bookingId,

        UUID eventId,

        BookingStatus status,

        int seatCount,

        BigDecimal totalAmount

) {}