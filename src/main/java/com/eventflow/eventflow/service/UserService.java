package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.request.CreateUserRequest;
import com.eventflow.eventflow.dto.response.UserResponse;
import com.eventflow.eventflow.entity.Role;
import com.eventflow.eventflow.entity.User;
import com.eventflow.eventflow.exception.UserAlreadyExistsException;
import com.eventflow.eventflow.kafka.KafkaEventProducer;
import com.eventflow.eventflow.kafka.event.UserRegisteredEvent;
import com.eventflow.eventflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaEventProducer kafkaEventProducer;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, KafkaEventProducer kafkaEventProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    /**
     * Creates a new user and returns a safe response DTO.
     */
    public UserResponse createUser(CreateUserRequest request) {

        // Business Validation
        // no same Ph number
        if (userRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "A user with this Phone Number already exists."
            );
        }
        // no same email
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "A user with this email already exists."
            );
        }

        // DTO -> Entity Mapping
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());

        // TODO: Encrypt password using BCrypt
        user.setPassword(passwordEncoder.encode(request.password()));

        user.setPhoneNumber(request.phoneNumber());
        user.setDateOfBirth(request.dateOfBirth());

        user.setEnabled(true);
        user.setRole(Role.USER);

        // TODO: Move timestamps to JPA lifecycle callbacks (@PrePersist)
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // Persist Entity
        User savedUser = userRepository.save(user);

        kafkaEventProducer.publishUserRegistered(
                new UserRegisteredEvent(
                        savedUser.getId(),
                        savedUser.getFirstName(),
                        savedUser.getEmail(),
                        Instant.now()
                )
        );

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