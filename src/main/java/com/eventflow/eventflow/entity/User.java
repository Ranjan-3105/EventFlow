    package com.eventflow.eventflow.entity;

    import jakarta.persistence.*;
    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    import java.time.Instant;
    import java.time.LocalDate;
    import java.util.UUID;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Entity
    @Table(name = "users")
    public class User {

        @Id
        private UUID id;

        @Column(nullable = false)
        private String firstName;

        @Column(nullable = false)
        private String lastName;

        @NotBlank
        @Column(nullable = false)
        private String password;

        @Column(nullable = false, unique = true)
        @Email
        private String email;

        @Column(nullable = false, unique = true)
        private String phoneNumber;

        private Instant createdAt;
        private Instant updatedAt;

        @Enumerated(EnumType.STRING)
        private Role role;

        private boolean enabled;

        @Column(nullable = false)
        private LocalDate dateOfBirth;

    }
