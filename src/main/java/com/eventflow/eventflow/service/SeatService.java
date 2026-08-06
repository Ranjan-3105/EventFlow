package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.request.GenerateSeatsRequest;
import com.eventflow.eventflow.dto.response.GenerateSeatsResponse;
import com.eventflow.eventflow.entity.Hall;
import com.eventflow.eventflow.entity.Seat;
import com.eventflow.eventflow.entity.SeatType;
import com.eventflow.eventflow.exception.HallNotFoundException;
import com.eventflow.eventflow.exception.InvalidSeatLayoutException;
import com.eventflow.eventflow.exception.SeatLayoutAlreadyExistsException;
import com.eventflow.eventflow.repository.HallRepository;
import com.eventflow.eventflow.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SeatService {

    private static final int MAX_ROWS = 26;
    private static final int MAX_SEATS_PER_ROW = 50;
    private final SeatRepository seatRepository;
    private final HallRepository hallRepository;

    public SeatService(
            SeatRepository seatRepository,
            HallRepository hallRepository
    ) {
        this.seatRepository = seatRepository;
        this.hallRepository = hallRepository;
    }

    public GenerateSeatsResponse generateSeats(
            UUID hallId,
            GenerateSeatsRequest request
    ) {

        Hall hall = hallRepository
                .findById(hallId)
                .orElseThrow(() ->
                        new HallNotFoundException(
                                "Hall not found"
                        ));

        if (seatRepository.existsByHallId(hallId)) {
            throw new SeatLayoutAlreadyExistsException(
                    "Seat layout already exists for this hall."
            );
        }

        if (request.rows() > MAX_ROWS) {
            throw new InvalidSeatLayoutException(
                    "Maximum 26 rows are allowed."
            );
        }

        if (request.seatsPerRow() > MAX_SEATS_PER_ROW) {
            throw new InvalidSeatLayoutException(
                    "Maximum 50 seats per row are allowed."
            );
        }

        List<Seat> seats = new ArrayList<>(
                request.rows() * request.seatsPerRow()
        );

        Instant now = Instant.now();

        for (int row = 0; row < request.rows(); row++) {

            char rowLabel = (char) ('A' + row);

            for (int seat = 1; seat <= request.seatsPerRow(); seat++) {

                Seat newSeat = new Seat();

                newSeat.setId(UUID.randomUUID());
                newSeat.setRowLabel(rowLabel);
                newSeat.setSeatNumber(seat);
                newSeat.setSeatType(SeatType.EXECUTIVE);
                newSeat.setHall(hall);
                newSeat.setCreatedAt(now);
                newSeat.setUpdatedAt(now);

                seats.add(newSeat);
            }
        }

        seatRepository.saveAll(seats);

        hall.setCapacity(seats.size());
        hall.setUpdatedAt(now);

        hallRepository.save(hall);

        return new GenerateSeatsResponse(
                hall.getId(),
                seats.size(),
                "Seats generated successfully."
        );
    }
}