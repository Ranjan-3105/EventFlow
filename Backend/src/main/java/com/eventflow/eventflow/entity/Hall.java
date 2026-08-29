package com.eventflow.eventflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "halls")
public class Hall {

    @Id
    private UUID id;

    @Column(nullable = false)
    private int hallNumber;

    @PositiveOrZero
    private int capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venueID")
    private Venue venue;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
