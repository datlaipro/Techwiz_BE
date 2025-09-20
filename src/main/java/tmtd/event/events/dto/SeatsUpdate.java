package tmtd.event.events.dto;

public record SeatsUpdate(
    Long eventId,
    Long totalSeats,
    Long seatsAvailable,
    long ts
) {}
