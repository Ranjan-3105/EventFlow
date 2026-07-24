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
    public UserResponse createUser(CreateUserRequest request) {

        // Business Validation
        // no same Ph number
        if (userRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new RuntimeException("Phone number already exists.");
        }
        // no same email
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException(
                    "User with email " + request.email() + " already exists."
            );
        }

        // DTO -> Entity Mapping
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());

        // TODO: Encrypt password using BCrypt
        user.setPassword(request.password());

        user.setPhoneNumber(request.phoneNumber());
        user.setDateOfBirth(request.dateOfBirth());

        user.setEnabled(true);
        user.setRole(Role.USER);

        // TODO: Move timestamps to JPA lifecycle callbacks (@PrePersist)
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // Persist Entity
        User savedUser = userRepository.save(user);

        // Entity -> Response DTO Mapping
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