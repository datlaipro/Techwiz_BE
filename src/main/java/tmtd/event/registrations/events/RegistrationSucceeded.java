package tmtd.event.registrations.events;

public record RegistrationSucceeded(
        Long registrationId,
        Long eventId,
        Long studentId,
        String studentEmail,
        String eventTitle
) {}
