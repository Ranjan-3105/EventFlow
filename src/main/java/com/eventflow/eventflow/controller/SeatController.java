package com.eventflow.eventflow.controller;

import com.eventflow.eventflow.dto.request.GenerateSeatsRequest;
import com.eventflow.eventflow.dto.response.GenerateSeatsResponse;
import com.eventflow.eventflow.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/halls")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/{hallId}/generate-seats")
    public ResponseEntity<GenerateSeatsResponse> generateSeats(
            @PathVariable UUID hallId,
            @Valid @RequestBody GenerateSeatsRequest request
    ) {

        GenerateSeatsResponse response =
                seatService.generateSeats(hallId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}