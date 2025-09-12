package tmtd.event.events.dto;

public record EventStatsResponse(
    Long eventId,
    long totalRegistrations,
    long checkedIn,
    long pending
) {}
