package tmtd.event.events.dto;

public record SeatsUpdate(
    Long eventId,
    int totalSeats,
    int seatsAvailable,
    long ts
) {}
