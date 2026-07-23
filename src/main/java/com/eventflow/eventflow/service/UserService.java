package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.request.CreateUserRequest;
import com.eventflow.eventflow.dto.response.UserResponse;
import com.eventflow.eventflow.entity.Role;
import com.eventflow.eventflow.entity.User;
import com.eventflow.eventflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user and returns a safe response DTO.
     */
    public UserResponse createUser(CreateUserRequest request){

        User data = new User();

        data.setId(UUID.randomUUID());
        data.setFirstName(request.firstName());
        data.setLastName(request.lastName());
        data.setEmail(request.email());

        // ADD Encryption here using Bcrypt
        data.setPassword(request.password());
        data.setPhoneNumber(request.phoneNumber());
        data.setDateOfBirth(request.dateOfBirth());
        data.setEnabled(true);
        data.setCreatedAt(Instant.now());
        data.setUpdatedAt(Instant.now());
        data.setRole(Role.USER);

        User savedUser = userRepository.save(data);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedUser.getRole()
        );
    }



}
