package com.eventflow.eventflow.controller;

import com.eventflow.eventflow.dto.request.CreateHallRequest;
import com.eventflow.eventflow.dto.response.HallResponse;
import com.eventflow.eventflow.service.HallService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/halls")
public class HallController {

    private final HallService hallService;

    public HallController(HallService hallService) {
        this.hallService = hallService;
    }

    @PostMapping
    public ResponseEntity<HallResponse> createHall(
            @Valid @RequestBody CreateHallRequest request
    ) {

        System.out.println(">>> HallController reached");

        HallResponse response =
                hallService.createHall(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}