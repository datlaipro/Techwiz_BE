package tmtd.event.registrations.dto;

import java.time.Instant;

import tmtd.event.registrations.RegistrationStatus;

public class RegistrationResponse {
    private Long registrationId;
    private Long eventId;
    private Long studentId;   // đổi Integer -> Long
    private RegistrationStatus status;
    private Instant registeredOn;

    public RegistrationResponse(Long registrationId, Long eventId, Long studentId,
                                RegistrationStatus status, Instant registeredOn) {
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.studentId = studentId;
        this.status = status;
        this.registeredOn = registeredOn;
    }

    public Long getRegistrationId() { return registrationId; }
    public Long getEventId() { return eventId; }
    public Long getStudentId() { return studentId; }
    public RegistrationStatus getStatus() { return status; }
    public Instant getRegisteredOn() { return registeredOn; }
}
