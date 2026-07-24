package com.eventflow.eventflow.controller;

import com.eventflow.eventflow.dto.request.CreateUserRequest;
import com.eventflow.eventflow.dto.response.UserResponse;
import com.eventflow.eventflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponse registerUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return userService.createUser(request);
    }
}